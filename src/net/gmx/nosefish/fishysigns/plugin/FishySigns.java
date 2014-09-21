package net.gmx.nosefish.fishysigns.plugin;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;

import net.canarymod.Canary;
import net.canarymod.api.world.Chunk;
import net.canarymod.api.world.UnknownWorldException;
import net.canarymod.api.world.World;
import net.canarymod.plugin.Plugin;
import net.canarymod.tasks.ServerTaskManager;
import net.canarymod.tasks.TaskOwner;
import net.gmx.nosefish.fishylib.worldmath.FishyChunk;
import net.gmx.nosefish.fishysigns.Log;
import net.gmx.nosefish.fishysigns.plugin.engine.ActivationManager;
import net.gmx.nosefish.fishysigns.plugin.engine.FishyEngineListener;
import net.gmx.nosefish.fishysigns.plugin.engine.FishySignClassLoader;
import net.gmx.nosefish.fishysigns.plugin.engine.FishySignFinderTask;
import net.gmx.nosefish.fishysigns.plugin.engine.ServerTicker;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.task.FishyTaskManager;
import net.gmx.nosefish.fishysigns.watcher.ChunkTracker;
import net.gmx.nosefish.fishysigns.watcher.IFishyWatcher;

public class FishySigns extends Plugin implements TaskOwner{
	private static final Set<IFishyWatcher> watchers = Collections.newSetFromMap(
	                                             new WeakHashMap<IFishyWatcher, Boolean>(8, 0.9f));
	private static volatile WeakReference<FishySigns> instance = new WeakReference<>(null);
	
	@Override
	public boolean enable() {
		instance = new WeakReference<>(this);
		Log.initialize(this);
		ServerTicker.getInstance().start();
		FishyTaskManager.initialize(this);
		// TODO: this isn't pretty, but we must load the classes here. Is there a better way?
		ActivationManager.getInstance().enable();
		ChunkTracker.getInstance().enable();
		enableWatchers();
		FishySignClassLoader.getInstance().loadAllFishySignClasses();
		findAllLoadedFishySigns();
		registerListeners();
		return true;
	}
	
	@Override
	public void disable() {
		instance = new WeakReference<>(null);
		disableWatchers();
		FishyTaskManager.getInstance().shutdown();
		ServerTicker.getInstance().shutdown();
		ServerTaskManager.removeTasks(this);
	}
	
	/**
	 * Usually called in a static initializer, but can be
	 * called any time, for example by a plugin that wants
	 * to add a custom watcher.
	 * 
	 * @param watcher
	 */
	public static void addWatcher(IFishyWatcher watcher) {
		Log.get().info("adding Watcher: " + watcher.getClass().getSimpleName());
		watchers.add(watcher);
		if (instance.get() != null) {
			watcher.enable();
			Log.get().info("registering Listener: " + watcher.getClass().getSimpleName());
			Canary.hooks().registerListener(watcher, instance.get());
		}
	}
	
	private void registerListeners() {
		Canary.hooks().registerListener(new FishyEngineListener(), this);
		// is there any conceivable case where this is needed?
//		for (PluginListener watcher : watchers) {
//			Log.get().logInfo("registering Listener: " + watcher.getClass().getSimpleName());
//			Canary.hooks().registerListener(watcher, this);
//		}
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
            try {
			World world = Canary.getServer().getWorldManager().getWorld(worldName, false);
			if (world == null) {
				continue;
			}
			List<Chunk> loadedChunks = world.getLoadedChunks();
			if (loadedChunks == null || loadedChunks.isEmpty()) {
				continue;
			}
			List<FishyChunk> loadedFishyChunks = new ArrayList<>(loadedChunks.size());
			for (Chunk chunk : loadedChunks) {
				ChunkTracker.getInstance().addChunk(chunk);
				loadedFishyChunks.add(new FishyChunk(chunk));
			}
			FishyTask signFinder = new FishySignFinderTask(loadedFishyChunks);
			signFinder.submit();
            } catch(UnknownWorldException e) {
                Log.get().warn(e);
            }
		}
	}
}
