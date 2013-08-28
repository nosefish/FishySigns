package net.gmx.nosefish.fishysigns.watcher;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;

import net.canarymod.api.world.blocks.Block;
import net.gmx.nosefish.fishysigns.activator.ActivatorRedstone;
import net.gmx.nosefish.fishysigns.activator.ImmutableRedstoneChange;
import net.gmx.nosefish.fishysigns.plugin.engine.ActivationManager;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;

public class RedstoneChangeWatcher extends BlockLocationWatcher{
	private static RedstoneChangeWatcher instance = new RedstoneChangeWatcher();

	private RedstoneChangeCollector rsChangeCollector = null;
	
	private RedstoneChangeWatcher() {
		
	}
	
	/**
	 * Called by FishyEventListener. Do not call yourself.
	 * @param block
	 * @param oldLevel
	 * @param newLevel
	 */
	public void redstoneChanged(Block block, int oldLevel, int newLevel) {
//		FishyLocationInt tmpLoc = new FishyLocationInt(block.getLocation());
//		System.out.println("RS change: " + block.getType().getMachineName() + " " + oldLevel + " -> " + newLevel);
//		System.out.println("changed   : " + tmpLoc + " - " + tmpLoc.hashCode());
//		for (FishyLocationInt l : this.blockLocationIndex.keySet()) {
//			System.out.println("registered: "+ l + " - " + l.hashCode());
//		}
		if (this.rsChangeCollector != null // started
				&& (oldLevel == 0) != (newLevel == 0) // high/low change, boolean != is XOR
				&& this.blockLocationIndex.containsKey(new FishyLocationInt(block.getLocation()))) {
//			System.out.println("RS change added to collector");
			this.rsChangeCollector.add(new ImmutableRedstoneChange(block, oldLevel, newLevel));
		}
	}
	
	public static RedstoneChangeWatcher getInstance() {
		return instance;
	}
	
	/**
	 * Starts the watcher. Called by the Plugin.
	 * Do not call from anywhere else.
	 */
	public void start() {
		if (this.rsChangeCollector != null) {
			this.rsChangeCollector.cancel();
		}
		this.rsChangeCollector = new RedstoneChangeCollector();
		this.rsChangeCollector.submit();
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
		private final AtomicReference<List<ImmutableRedstoneChange>> changeBuffer;
		
		public RedstoneChangeCollector() {
			this.setTickDelay(0);
			this.setTickRepeatDelay(1);
			changeBuffer = 
					new AtomicReference<List<ImmutableRedstoneChange>>(
							new LinkedList<ImmutableRedstoneChange>());
		}		
		public void add(ImmutableRedstoneChange rsChange) {
			changeBuffer.get().add(rsChange);
		}
		
		public void doStuff(){
			this.setNextTask(null);
			List<ImmutableRedstoneChange> lastTickChanges = changeBuffer.get();
			if (! lastTickChanges.isEmpty()) {
				changeBuffer.set(new LinkedList<ImmutableRedstoneChange>());
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
		private final List<ImmutableRedstoneChange> changes;
		
		public ActivationTask(List<ImmutableRedstoneChange> changes) {
			super();
			this.setThreadsafe_IPromiseThatThisDoesNotTouchTheWorld();
			this.changes = changes;
		}
		
		@Override
		public void doStuff() {
			Map<Long, ActivatorRedstone> activators = new TreeMap<Long, ActivatorRedstone>();
			// group changes by ids to activate
			for (ImmutableRedstoneChange change : changes) {
				Set<Long> idsToActivate = blockLocationIndex.get(change.getLocation());
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
