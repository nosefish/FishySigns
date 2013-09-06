package net.gmx.nosefish.fishysigns.plugin.engine;


import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.blocks.Sign;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.player.ConnectionHook;
import net.canarymod.hook.player.SignChangeHook;
import net.canarymod.hook.system.ServerTickHook;
import net.canarymod.plugin.PluginListener;
import net.canarymod.plugin.Priority;
import net.gmx.nosefish.fishysigns.signs.FishySign;
import net.gmx.nosefish.fishysigns.task.FishyTaskManager;


/**
 * @author Stefan Steinheimer (nosefish)
 *
 */
public class FishyEngineListener implements PluginListener {

	@HookHandler(priority=Priority.PASSIVE)
	public void onLogin(ConnectionHook hook) {
		hook.getPlayer().message("Something is fishy about the signs on this server...");
	}
	
	@HookHandler(priority=Priority.PASSIVE)
	public void onSignCreation(SignChangeHook hook) {
		Sign sign = hook.getSign();
		Player player = hook.getPlayer();
		FishySign.createAndRegister(sign, player);
	}
	
	@HookHandler(priority=Priority.PASSIVE)
	public void tick(ServerTickHook hook) {
		ServerTicker.getInstance().tick();
		FishyTaskManager.getInstance().tick();
	}
}
