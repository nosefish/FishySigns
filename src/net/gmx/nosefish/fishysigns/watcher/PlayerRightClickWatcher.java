package net.gmx.nosefish.fishysigns.watcher;

import java.util.Set;

import net.canarymod.api.world.blocks.Block;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.player.BlockRightClickHook;
import net.canarymod.plugin.Priority;
import net.gmx.nosefish.fishysigns.activator.Activator;
import net.gmx.nosefish.fishysigns.activator.ActivatorPlayerRightClick;
import net.gmx.nosefish.fishysigns.plugin.FishySigns;
import net.gmx.nosefish.fishysigns.plugin.engine.ActivationManager;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.world.ImmutableLocationBlockState;


public class PlayerRightClickWatcher extends BlockLocationWatcher{
	private static final PlayerRightClickWatcher instance = new PlayerRightClickWatcher();
	
	static{
		FishySigns.addWatcher(instance);
	}

	private PlayerRightClickWatcher() {
		// nothing to do
	}
	
	@HookHandler(priority=Priority.PASSIVE)
	public void onRightClick(BlockRightClickHook hook){
		if (! enabled) {
			return;
		}
		Block block = hook.getBlockClicked();
		String playerName = hook.getPlayer().getName();
		FishyTask activate = new ActivationTask(new ImmutableLocationBlockState(block), playerName);
		activate.submit();
	}

	public static PlayerRightClickWatcher getInstance() {
		return instance;
	}
	
	
	private class ActivationTask extends FishyTask{
		private final ImmutableLocationBlockState blockState;
		private final String playerName;
		public ActivationTask(ImmutableLocationBlockState blockState,
				String playerName){
			this.setThreadsafe_IPromiseThatThisDoesNotTouchTheWorld();
			this.blockState = blockState;
			this.playerName = playerName;
		}
		
		@Override
		public void doStuff() {
			Set<Long> idSet = blockLocationIndex.get(blockState.getLocation());
			if (idSet != null) {
				Activator activator = new ActivatorPlayerRightClick(playerName, blockState);
				synchronized(idSet) {
					for (long id : idSet) {
						ActivationManager.getInstance().activate(id, activator);
					}
				}
			}
		}
	}// end of internal class


}
