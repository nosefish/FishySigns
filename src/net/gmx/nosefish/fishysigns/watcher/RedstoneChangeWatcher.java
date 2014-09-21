package net.gmx.nosefish.fishysigns.watcher;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import net.canarymod.api.world.blocks.Block;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.world.RedstoneChangeHook;
import net.canarymod.plugin.Priority;
import net.gmx.nosefish.fishysigns.Log;
import net.gmx.nosefish.fishysigns.exception.DisabledException;
import net.gmx.nosefish.fishysigns.plugin.FishySigns;
import net.gmx.nosefish.fishysigns.plugin.engine.ActivationManager;
import net.gmx.nosefish.fishysigns.plugin.engine.ServerTicker;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.watcher.activator.ActivatorRedstone;
import net.gmx.nosefish.fishysigns.watcher.activator.FishyRedstoneChange;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;

public class RedstoneChangeWatcher extends BlockLocationWatcher{
	private static final RedstoneChangeWatcher instance = new RedstoneChangeWatcher();
	static {
		FishySigns.addWatcher(instance);
	}
	
	private RedstoneChangeCollector rsChangeCollector = null;
	
	private RedstoneChangeWatcher() {
		
	}
	
	@Override
	public void enable() {
		super.enable();
		if (this.rsChangeCollector != null) {
			this.rsChangeCollector.cancel();
		}
		this.rsChangeCollector = new RedstoneChangeCollector();
		this.rsChangeCollector.submit();
	}
	
    @Override
	public void disable() {
		super.disable();
		this.rsChangeCollector.cancel();
		this.rsChangeCollector = null;
	}
	
	@HookHandler(priority=Priority.PASSIVE)
	public void redstoneChanged(RedstoneChangeHook hook) {
		if (! enabled) {
			return;
		}
		Block block = hook.getSourceBlock();
		int oldLevel = hook.getOldLevel();
		int newLevel = hook.getNewLevel();
		long tickStamp;
		try {
			tickStamp = ServerTicker.getInstance().getTickCount();
		} catch (DisabledException e) {
			Log.get().warn("RedstoneChangeWatcher: the ServerTicker is disabled, ignoring hook call");
			return;
		}
		if ((oldLevel == 0) != (newLevel == 0) // high/low change, boolean != is XOR
			&& this.blockLocationIndex.containsKey(new FishyLocationInt(block.getLocation()))) {
			this.rsChangeCollector.add(new FishyRedstoneChange(block, oldLevel, newLevel, tickStamp));
		}
	}
	
	public static RedstoneChangeWatcher getInstance() {
		return instance;
	}
	
	
	/**
	 * Collects all redstone changes that arrive during one tick.
	 * At the end of each tick it creates an <code>ActivationTask</code>
	 * with the changes and resets the collection list.
	 * 
	 * @author Stefan Steinheimer (nosefish)
	 *
	 */
	private class RedstoneChangeCollector extends FishyTask {
		private final AtomicReference<List<FishyRedstoneChange>> changeBuffer;
		
		public RedstoneChangeCollector() {
			this.setTickDelay(0);
			this.setTickRepeatDelay(1);
			changeBuffer = 
					new AtomicReference<List<FishyRedstoneChange>>(
							new LinkedList<FishyRedstoneChange>());
		}		
		public void add(FishyRedstoneChange rsChange) {
			changeBuffer.get().add(rsChange);
		}
		
        @Override
		public void doStuff(){
			this.setNextTask(null);
			List<FishyRedstoneChange> lastTickChanges = changeBuffer.get();
			if (! lastTickChanges.isEmpty()) {
				changeBuffer.set(new LinkedList<FishyRedstoneChange>());
				this.setNextTask(new ActivationTask(lastTickChanges));
			}
		}
	}// end of internal class


	/**
	 * Prepares activators with all collected changes by id and 
	 * activates the <code>Activatables</code> that
	 * are registered for them.
	 * 
	 * @author Stefan Steinheimer (nosefish)
	 *
	 */
	private class ActivationTask extends FishyTask {
		private final List<FishyRedstoneChange> changes;
		
		public ActivationTask(List<FishyRedstoneChange> changes) {
			super();
			this.setThreadsafe_IPromiseThatThisDoesNotTouchTheWorld();
			this.changes = changes;
		}
		
		@Override
		public void doStuff() {
			Map<Long, ActivatorRedstone> activators = new TreeMap<>();
			// group changes by ids to activate
			for (FishyRedstoneChange change : changes) {
				Set<Long> idsToActivate = blockLocationIndex.get(change.getLocation());
				if (idsToActivate == null) {
					continue;
				}
				// synchronizedSet mandates synchronization when iterating
				synchronized(idsToActivate) {
					for (long id : idsToActivate) {
						if (! activators.containsKey(id)) {
							activators.put(id, new ActivatorRedstone());
						}
						activators.get(id).addChange(change);
					}
				}
			}
			ActivationManager.getInstance().activateAll(activators);
		}
	} // end of internal class
}
