package net.gmx.nosefish.fishysigns.plugin.engine;

import net.canarymod.hook.HookHandler;
import net.canarymod.hook.player.BlockRightClickHook;
import net.canarymod.hook.world.BlockUpdateHook;
import net.canarymod.hook.world.RedstoneChangeHook;
import net.canarymod.plugin.PluginListener;
import net.canarymod.plugin.Priority;
import net.gmx.nosefish.fishysigns.watcher.PlayerRightClickWatcher;
import net.gmx.nosefish.fishysigns.watcher.RedstoneChangeWatcher;

/**
 * Handles events that activate FishySigns
 *
 * @author Stefan Steinheimer (nosefish)
 *
 */
public class FishyEventListener implements PluginListener {

	@HookHandler(priority=Priority.PASSIVE)
	public void blockRightClicked(BlockRightClickHook hook) {
		PlayerRightClickWatcher.getInstance().onRightClick(hook.getBlockClicked(), hook.getPlayer().getName());
	}

	@HookHandler(priority=Priority.PASSIVE)
	public void redstoneChanged(RedstoneChangeHook hook) {
		RedstoneChangeWatcher.getInstance().redstoneChanged(
				hook.getSourceBlock(),
				hook.getOldLevel(),
				hook.getNewLevel()
				);
	}

	
	@HookHandler(priority=Priority.PASSIVE)
	public void updateTest(BlockUpdateHook hook) {
		//System.out.println("BlockUpdateHook: " + hook);
	}
}
