package net.gmx.nosefish.fishysigns.plugin;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import net.canarymod.Canary;
import net.canarymod.api.world.Chunk;
import net.canarymod.api.world.World;
import net.canarymod.plugin.Plugin;
import net.canarymod.plugin.PluginListener;
import net.canarymod.tasks.ServerTaskManager;
import net.canarymod.tasks.TaskOwner;
import net.gmx.nosefish.fishylib.worldmath.FishyChunk;
import net.gmx.nosefish.fishysigns.Log;
import net.gmx.nosefish.fishysigns.plugin.engine.FishyEngineListener;
import net.gmx.nosefish.fishysigns.plugin.engine.FishySignClassLoader;
import net.gmx.nosefish.fishysigns.plugin.engine.FishySignFinderTask;
import net.gmx.nosefish.fishysigns.plugin.engine.ServerTicker;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.task.FishyTaskManager;
import net.gmx.nosefish.fishysigns.watcher.IFishyWatcher;
import net.gmx.nosefish.fishysigns.world.ChunkTracker;

public class FishySigns extends Plugin implements TaskOwner{
	private static Set<IFishyWatcher> watchers = Collections.newSetFromMap(
	                                             new WeakHashMap<IFishyWatcher, Boolean>(8, 0.9f));
	private static boolean enabled = false;
	private static WeakReference<FishySigns> instance = new WeakReference<FishySigns>(null);
	
	@Override
	public boolean enable() {
		instance = new WeakReference<FishySigns>(this);
		Log.initialize(this);
		ServerTicker.getInstance().start();
		FishyTaskManager.initialize(this);
		ChunkTracker.getInstance().clear();
		enableWatchers();
		FishySignClassLoader.getInstance().loadAllFishySignClasses();
		findAllLoadedFishySigns();
		registerListeners();
		enabled = true;
		return true;
	}
	
	@Override
	public void disable() {
		enabled = false;
		new WeakReference<FishySigns>(null);
		disableWatchers();
		FishyTaskManager.getInstance().shutdown();
		ServerTicker.getInstance().shutdown();
		ServerTaskManager.removeTasksForPlugin(this);
		ChunkTracker.getInstance().clear();
	}
	
	/**
	 * Usually called in a static initializer, but can be
	 * called at runtime, for example by a plugin that wants
	 * to add a custom watcher.
	 * 
	 * @param watcher
	 */
	public static void addWatcher(IFishyWatcher watcher) {
		watchers.add(watcher);
		if (enabled) {
			watcher.enable();
			Canary.hooks().registerListener(watcher, instance.get());
		}
	}
	
	private void registerListeners() {
		Canary.hooks().registerListener(new FishyEngineListener(), this);
		for (PluginListener watcher : watchers) {
			Canary.hooks().registerListener(watcher, this);
		}
	}
	
	private void enableWatchers() {
		for (IFishyWatcher watcher : watchers) {
			watcher.enable();
		}
	}
	
	private void disableWatchers() {
		for (IFishyWatcher watcher : watchers) {
			watcher.disable();
		}
	}
	
	/**
	 * Finds and loads FishySigns in all loaded chunks to
	 * make enable (re)loading the plugin when the server is running.
	 */
	private void findAllLoadedFishySigns() {
		List<String> allWorlds = Canary.getServer().getWorldManager().getExistingWorlds();
		for (String worldName: allWorlds) {
			World world = Canary.getServer().getWorldManager().getWorld(worldName, false);
			if (world == null) {
				continue;
			}
			List<Chunk> loadedChunks = world.getLoadedChunks();
			if (loadedChunks == null || loadedChunks.isEmpty()) {
				continue;
			}
			List<FishyChunk> loadedFishyChunks = new ArrayList<FishyChunk>(loadedChunks.size());
			for (Chunk chunk : loadedChunks) {
				ChunkTracker.getInstance().add(chunk);
				loadedFishyChunks.add(new FishyChunk(chunk));
			}
			FishyTask signFinder = new FishySignFinderTask(loadedFishyChunks);
			signFinder.submit();
		}
	}

}
