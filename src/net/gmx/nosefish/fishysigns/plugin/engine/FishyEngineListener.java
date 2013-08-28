package net.gmx.nosefish.fishysigns.plugin.engine;


import java.util.Set;

import net.gmx.nosefish.fishylib.worldmath.FishyChunk;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.blocks.Sign;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.player.BlockDestroyHook;
import net.canarymod.hook.player.ConnectionHook;
import net.canarymod.hook.player.SignChangeHook;
import net.canarymod.hook.system.ServerTickHook;
import net.canarymod.hook.world.ChunkLoadedHook;
import net.canarymod.hook.world.ChunkUnloadHook;
import net.canarymod.plugin.PluginListener;
import net.canarymod.plugin.Priority;
import net.gmx.nosefish.fishysigns.signs.FishySign;
import net.gmx.nosefish.fishysigns.task.FishyTaskManager;
import net.gmx.nosefish.fishysigns.world.ChunkTracker;

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
	public void onChunkLoaded(ChunkLoadedHook hook) {
		Set<FishyChunk> fullyLoadedChunks = ChunkTracker.getInstance().add(hook.getChunk());
		if (!fullyLoadedChunks.isEmpty()) {
			FishySignFinderTask fsFinder = new FishySignFinderTask(fullyLoadedChunks);
			fsFinder.submit();
		}
		
	}
	
	@HookHandler(priority=Priority.PASSIVE)
	public void onChunkUnloaded(ChunkUnloadHook hook) {
		Set<FishyChunk> toUnload = ChunkTracker.getInstance().remove(hook.getChunk());
		for (FishyChunk fChunk : toUnload) {
			ActivationManager.getInstance().removeAllInChunk(fChunk);
		}
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

	@HookHandler(priority=Priority.PASSIVE)
	public void onBlockDestruction(BlockDestroyHook hook) {
		ActivationManager.getInstance().remove(new FishyLocationInt(hook.getBlock().getLocation()));
	}

}
