package net.gmx.nosefish.fishysigns.watcher;

import java.util.Set;

import net.canarymod.hook.HookHandler;
import net.canarymod.hook.world.BlockUpdateHook;
import net.canarymod.plugin.Priority;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.plugin.FishySigns;
import net.gmx.nosefish.fishysigns.plugin.engine.ActivationManager;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.watcher.activator.IActivator;
import net.gmx.nosefish.fishysigns.world.FishyBlockState;

public class BlockDestructionWatcher extends BlockLocationWatcher {
	private static BlockDestructionWatcher instance = 
			new BlockDestructionWatcher();
	static {
		FishySigns.addWatcher(instance);
	}
	
	/**
	 * Called by Canary
	 * 
	 * @param hook
	 */
	@HookHandler(priority=Priority.PASSIVE)
	public void onBlockDestruction(BlockUpdateHook hook) {
		if (! enabled) {
			return;
		}
		if (hook.getNewBlockId() == hook.getBlock().getTypeId()) {
			// nothing was destroyed
			return;
		}
		FishyLocationInt location = new FishyLocationInt(hook.getBlock().getLocation());
		if (blockLocationIndex.containsKey(location)) {
			ActivatorBlockDestroyed activator = new ActivatorBlockDestroyed(location, 
					new FishyBlockState(hook.getBlock()));
			ActivationTask task = 
					new ActivationTask(activator);
			task.submit();
		}
	}
	
	public static BlockDestructionWatcher getInstance() {
		return instance;
	}

	/**
	 * Handles the activation
	 * 
	 * @author Stefan Steinheimer (nosefish)
	 */
	private class ActivationTask extends FishyTask {
		private final ActivatorBlockDestroyed activator;
		
		public ActivationTask(ActivatorBlockDestroyed activator) {
			this.activator = activator;
			this.setThreadsafe_IPromiseThatThisDoesNotTouchTheWorld();
		}
		
		@Override
		public void doStuff() {
			Set<Long> idSet = blockLocationIndex.get(activator.getLocation());
			if (idSet == null || idSet.isEmpty()) {
				return;
			}
			Long[] toActivate = null;
			synchronized(idSet) {
				toActivate = idSet.toArray(new Long[idSet.size()]);
			}
			ActivationManager.getInstance().activateAll(activator, toActivate);
		}
	}
	
	/**
	 * Will be sent to the registered <code>Activatables</code>
	 * when a block they are interested in is destroyed.
	 * 
	 * @author Stefan Steinheimer (nosefish)
	 *
	 */
	public static class ActivatorBlockDestroyed implements IActivator {
		private final FishyLocationInt location;
		private final FishyBlockState blockState;
		
		public ActivatorBlockDestroyed(FishyLocationInt location, FishyBlockState blockState) {
			this.location = location;
			this.blockState = blockState;
		}
		
		/**
		 * @return the location
		 */
		public FishyLocationInt getLocation() {
			return location;
		}

		/**
		 * @return the block state
		 */
		public FishyBlockState getBlockState() {
			return blockState;
		}
		
	}
}
