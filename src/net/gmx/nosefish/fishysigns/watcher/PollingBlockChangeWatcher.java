package net.gmx.nosefish.fishysigns.watcher;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.gmx.nosefish.fishylib.worldmath.FishyChunk;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.system.ServerTickHook;
import net.canarymod.plugin.Priority;
import net.gmx.nosefish.fishysigns.plugin.FishySigns;
import net.gmx.nosefish.fishysigns.plugin.engine.ActivationManager;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.watcher.activator.ActivatorBlocks;
import net.gmx.nosefish.fishysigns.watcher.activator.FishyBlockStateChange;
import net.gmx.nosefish.fishysigns.world.FishyBlockState;


/**
 * Watches blocks for changes and activates Activatables when blocks they're
 * interested in have changed. It reliably catches every change in 
 * type or data value of watched blocks, at the expense of performance.
 * <p>
 * Because it polls all registered blocks regularly, this watcher is quite resource intensive.
 * A typical server will still be able to handle a few thousand registered blocks without lag.
 * (10000 blocks in 6ms on a Core 2 Quad Q9550, Java 7 on Linux in a VirtualBox VM)
 * Use one of the other watchers over this if they will do the job.
 * 
 * Extension of this class is not supported.
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 */
public final class PollingBlockChangeWatcher extends BlockLocationWatcher{
	private static PollingBlockChangeWatcher instance = new PollingBlockChangeWatcher();	
	static{
		FishySigns.addWatcher(instance);
	}

	// the blocks we're watching, and the state they had the last time we looked
	private final ConcurrentMap<FishyLocationInt, FishyBlockState> worldBlockStates;

	/**
	 * Private Constructor - this is a Singleton with a static initializer.
	 * Use <code>getInstance</code> to access it.
	 */
	private PollingBlockChangeWatcher() {
		this.worldBlockStates = new ConcurrentHashMap<FishyLocationInt, FishyBlockState>(64, 0.9F, 2);
	}


	/**
	 * Gets the singleton instance
	 * 
	 * @return the instance
	 */
	public static PollingBlockChangeWatcher getInstance() {
		return instance;
	}


	/**
	 * After registering, the <code>Activatable</code> will be activated whenever
	 * the block at the location changes.
	 * Shortly after registering, it will receive an <code>Activator</code> containing
	 * the current state and an invalid old state of (-1, -1).
	 * It will also receive an <code>Activator</code> like that when another
	 * <code>Activatable</code> registers for the same block later.
	 * Make sure to handle them.
	 *
	 * @param id
	 * @param location
	 */
	@Override
	public void register(Long activatableID, FishyLocationInt location) {
		synchronized(this) {
			super.register(activatableID, location);
			// Put an invalid block state into the map
			// to make it send the current block state
			// to the Activatable on the next check pass.
			FishyBlockState forceUpdate = new FishyBlockState((short)-1, (short)-1);
			worldBlockStates.put(location, forceUpdate);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void remove(Long activatableID) {
		synchronized(this) {
			List <FishyLocationInt> removedKeys = blockLocationIndex.removeValue(activatableID);	
			for (FishyLocationInt loc : removedKeys) {
				worldBlockStates.remove(loc);
			}
		}
	}
	
	@HookHandler(priority=Priority.PASSIVE)
	public void onTick(ServerTickHook hook) {
		if (! enabled) {
			return;
		}
		List<FishyBlockStateChange> changes = pollBlockStates();
		if (! changes.isEmpty()) {
			ActivationTask activate = new ActivationTask(changes);
			activate.submit();
		}
	}


	private List<FishyBlockStateChange> pollBlockStates() {
		List<FishyBlockStateChange> changes = new LinkedList<FishyBlockStateChange>();
		if(worldBlockStates.isEmpty()) {
			return changes;
		}
		for (FishyLocationInt location : worldBlockStates.keySet()) {
			World world = location.getWorld().getWorldIfLoaded();
			if (world == null) continue;
			if (! world.isChunkLoaded(FishyChunk.worldToChunk(location.getIntX()), FishyChunk.worldToChunk(location.getIntZ()))) {
				continue;
			}
			// world and chunk are loaded, let's check out this block
			Block block = world.getBlockAt(location.getIntX(), location.getIntY(), location.getIntZ());
			FishyBlockState oldState = worldBlockStates.get(location);
			if (oldState != null && ! oldState.equalsBlock(block)) {
				// If we're unlucky, the entry has been removed since we retrieved the value.
				// In that case, there's no point to process it further - nobody cares about
				// the block anymore.
				FishyBlockState newState = new FishyBlockState(block);
				// update the block state in the map
				FishyBlockState stillRelevant = worldBlockStates.replace(location, newState);
				if (stillRelevant != null) {
					// if it hasn't been removed, remember this as changed for later processing
					changes.add(new FishyBlockStateChange(location, oldState, newState));
				}
			}
		}
		return changes;
	}


	@Override
	public void disable() {
		super.disable();
		worldBlockStates.clear();
	}

	/**
	 * Activates the registered IDs outside the server thread
	 * @author Stefan Steinheimer (nosefish)
	 *
	 */
	private class ActivationTask extends FishyTask {
		private final List<FishyBlockStateChange> changes;

		private ActivationTask(List<FishyBlockStateChange> changes) {
			super();
			this.setThreadsafe_IPromiseThatThisDoesNotTouchTheWorld();
			this.changes = changes;
		}

		@Override
		public void doStuff() {
			Map<Long, ActivatorBlocks> toActivate = new TreeMap<Long, ActivatorBlocks>();
			for (FishyBlockStateChange change : changes) {
				// find out who is interested in this change
				Set<Long> recipients = PollingBlockChangeWatcher.this.blockLocationIndex.get(change.getLocation());
				if (recipients == null) {
					continue;
				}
				synchronized(recipients) {
					for (Long id : recipients) {
						// add change to recipient's Activator
						if (! toActivate.containsKey(id)) {
							toActivate.put(id, new ActivatorBlocks());
						}
						toActivate.get(id).add(change);
					}
				}
			}
			ActivationManager.getInstance().activateAll(toActivate);
		}
	}
}
