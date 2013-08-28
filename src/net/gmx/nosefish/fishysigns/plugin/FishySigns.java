package net.gmx.nosefish.fishysigns.plugin;

import net.canarymod.Canary;
import net.canarymod.plugin.Plugin;
import net.canarymod.tasks.ServerTaskManager;
import net.canarymod.tasks.TaskOwner;
import net.gmx.nosefish.fishysigns.Log;
import net.gmx.nosefish.fishysigns.plugin.engine.FishyEngineListener;
import net.gmx.nosefish.fishysigns.plugin.engine.FishyEventListener;
import net.gmx.nosefish.fishysigns.plugin.engine.FishySignClassLoader;
import net.gmx.nosefish.fishysigns.plugin.engine.ServerTicker;
import net.gmx.nosefish.fishysigns.task.FishyTaskManager;
import net.gmx.nosefish.fishysigns.watcher.PollingBlockChangeWatcher;
import net.gmx.nosefish.fishysigns.watcher.RedstoneChangeWatcher;
import net.gmx.nosefish.fishysigns.world.ChunkTracker;

public class FishySigns extends Plugin implements TaskOwner{

	@Override
	public boolean enable() {
		Log.initialize(this);
		ServerTicker.getInstance().start();
		FishyTaskManager.initialize(this);
		ChunkTracker.getInstance().clear();
		startWatchers();
		FishySignClassLoader.getInstance().loadAllFishySignClasses();
		registerListeners();
		return true;
	}
	
	@Override
	public void disable() {
		FishyTaskManager.getInstance().shutdown();
		ServerTicker.getInstance().shutdown();
		ServerTaskManager.removeTasksForPlugin(this);
		ChunkTracker.getInstance().clear();
	}
	
	private void registerListeners() {
		Canary.hooks().registerListener(new FishyEngineListener(), this);
		Canary.hooks().registerListener(new FishyEventListener(), this);
	}
	
	private void startWatchers() {
		PollingBlockChangeWatcher.getInstance().start();
		RedstoneChangeWatcher.getInstance().start();
	}

}
