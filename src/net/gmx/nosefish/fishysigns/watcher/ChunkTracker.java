package net.gmx.nosefish.fishysigns.watcher;
//TODO: move to engine package

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import net.gmx.nosefish.fishylib.worldmath.FishyChunk;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishylib.worldmath.FishyVectorInt;
import net.gmx.nosefish.fishysigns.plugin.FishySigns;
import net.gmx.nosefish.fishysigns.plugin.engine.ActivationManager;
import net.gmx.nosefish.fishysigns.plugin.engine.FishySignFinderTask;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.watcher.activator.ActivatorChunkUnloaded;

import net.canarymod.api.world.Chunk;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.world.ChunkLoadedHook;
import net.canarymod.hook.world.ChunkUnloadHook;
import net.canarymod.plugin.Priority;

/**
 * Provides a thread-safe way to determine whether a chunk or a chunk cluster is loaded.
 * A chunk cluster means a chunk and the 8 chunks surrounding it.
 * 
 * Also acts as a watcher for chunk unload events.
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 */
public final class ChunkTracker extends BlockLocationWatcher {
	private static ChunkTracker instance = new ChunkTracker();
	static{
		FishySigns.addWatcher(instance);
	}
	
	private static final FishyVectorInt[] clusterOffsets = new FishyVectorInt[]{
		new FishyVectorInt(16, 0, 0),
		new FishyVectorInt(-16, 0, 0),
		new FishyVectorInt(0, 0, 16),
		new FishyVectorInt(0, 0, -16),
		new FishyVectorInt(16, 0, 16),
		new FishyVectorInt(-16, 0, 16),
		new FishyVectorInt(16, 0, -16),
		new FishyVectorInt(-16, 0, -16)	
	};

	// the Boolean is true when all surrounding chunks are loaded, too
	private ConcurrentMap<FishyChunk, Boolean> loadedChunks;
	
	public static ChunkTracker getInstance() {
		return instance;
	}

	//TODO: multi-threaded chunk loading/unloading will probably break
	//      addChunk and removeChunk when it's added to Canary. Locking
	//      may be required to ensure correctness.
	private ChunkTracker() {
		this.loadedChunks =
				new ConcurrentHashMap<FishyChunk, Boolean>(256, 0.9F, 2);
	}
	
	/**
	 * Check if a chunk is loaded.
	 * 
	 * @param chunkLocation the chunk to check
	 * @return <code>true</code> if the chunk is loaded, <code>false</code> otherwise
	 */
	public boolean isChunkLoaded(FishyChunk chunkLocation) {
		return loadedChunks.containsKey(chunkLocation);
	}
	
	/**
	 * Check if the chunk that contains the block is loaded.
	 * 
	 * @param blockLocation the block to check
	 * @return <code>true</code> if the chunk is loaded, <code>false</code> otherwise
	 */
	public boolean isChunkLoaded(FishyLocationInt blockLocation) {
		return loadedChunks.containsKey(FishyChunk.getChunkContaining(blockLocation));
	}
	

	
	public boolean isChunkClusterLoaded(FishyChunk chunkLocation) {
		return loadedChunks.get(chunkLocation);		
	}
	
	public boolean isChunkClusterLoaded(FishyLocationInt blockLocation) {
		return isChunkClusterLoaded(FishyChunk.getChunkContaining(blockLocation));		
	}

	/**
	 * Adds a chunk to the tracker.
	 * Called by the FishySigns plugin class and the onChunkLoaded method.
	 * Do not call from anywhere else.
	 * 
	 * @param chunk the chunk to add.
	 * @return the chunks in the cluster that are now in a completely loaded cluster 
	 */
	public Set<FishyChunk> addChunk(Chunk chunk) {
		Set<FishyChunk> loadedClusters = new HashSet<FishyChunk>(10, 0.9F);
		FishyChunk newChunk = new FishyChunk(chunk);
		boolean neighboursLoaded = areAllNeighbouringChunksLoaded(newChunk);
		loadedChunks.put(newChunk, neighboursLoaded);
		//TODO: more efficient implementation maybe?
		if (neighboursLoaded) {
			loadedClusters.add(newChunk);
		}
		for (FishyVectorInt offset : clusterOffsets) {
			FishyChunk toCheck = FishyChunk.getChunkContaining(newChunk.addIntVector(offset));
			if (isChunkLoaded(toCheck) && areAllNeighbouringChunksLoaded(toCheck)) {
				loadedChunks.put(toCheck, true);
				loadedClusters.add(toCheck);
			}
		}
		return loadedClusters;
	}
	
	/**
	 * Checks whether all 8 neighbours of the chunk are loaded.
	 * @param chunkLocation
	 * @return <code>true if  all its neighbour chunks are loaded, <code>false</code> otherwise
	 */
	private boolean areAllNeighbouringChunksLoaded(FishyChunk chunkLocation) {
		for (FishyVectorInt offset : clusterOffsets) {
			if (!loadedChunks.containsKey(FishyChunk.getChunkContaining(chunkLocation.addIntVector(offset)))) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Removes a chunk from the tracker.
	 * Called by the FishyEngineListener. Do not call from anywhere else.
	 * 
	 * @param chunk the chunk to remove.
	 * @return Set of chunks that were surrounded by loaded chunks before and no longer are
	 */
	private Set<FishyChunk> removeChunk(Chunk chunk) {
		Set<FishyChunk> unloadedClusters = new HashSet<FishyChunk>(10, 0.9F);
		FishyChunk removedChunk = new FishyChunk(chunk);
		Boolean wasInCluster = loadedChunks.get(removedChunk);
		if (wasInCluster != null && wasInCluster) {
			unloadedClusters.add(removedChunk);
		}
		loadedChunks.remove(new FishyChunk(chunk));
		for (FishyVectorInt offset : clusterOffsets) {
			FishyChunk noLongerInCluster = FishyChunk.getChunkContaining(removedChunk.addIntVector(offset));
			wasInCluster = loadedChunks.get(noLongerInCluster);
			if (wasInCluster != null && wasInCluster) {
				loadedChunks.put(noLongerInCluster, false);
				unloadedClusters.add(noLongerInCluster);
			}
		}
		return unloadedClusters;
	}
	
	@HookHandler(priority=Priority.PASSIVE)
	public void onChunkLoaded(ChunkLoadedHook hook) {
		Set<FishyChunk> fullyLoadedChunks = this.addChunk(hook.getChunk());
		if (!fullyLoadedChunks.isEmpty()) {
			// TODO: move this somewhere else, it doesn't belong here
			// make an ActivatorChunkLoaded, put it in an Activatable - FishySigns would do nicely
			FishySignFinderTask fsFinder = new FishySignFinderTask(fullyLoadedChunks);
			fsFinder.submit();
		}
	}
	
	@HookHandler(priority=Priority.PASSIVE)
	public void onChunkUnloaded(ChunkUnloadHook hook) {
		Set<FishyChunk> toUnload = this.removeChunk(hook.getChunk());
		ActivationTask task = new ActivationTask(toUnload);
		task.submit();
	}

	@Override
	public void enable() {
		// nothing to do
	}

	@Override
	public void disable() {
		super.disable();
		loadedChunks.clear();
	}
	
	
	public void register(Long id, FishyLocationInt location) {
		blockLocationIndex.put(FishyChunk.getChunkContaining(location), id);
	}
	
	public void remove(Long id) {
		
	}
	
	/**
	 * An asynchronous <code>FishyTask</code> that removes all
	 * <code>Acivatable</code>s in the specified chunks from the indices.
	 * 
	 * @author Stefan Steinheimer
	 *
	 */
	private class ActivationTask extends FishyTask {
		Set<FishyChunk> chunks;

		public ActivationTask(Set<FishyChunk> chunks) {
			this.setThreadsafe_IPromiseThatThisDoesNotTouchTheWorld();
			this.chunks = chunks;
		}

		@Override
		public void doStuff() {
			for (FishyChunk chunk: chunks) {
				Set<Long> idSet = blockLocationIndex.get(chunk);
				if (idSet == null || idSet.isEmpty()) {
					return;
				}
				Long[] toActivate = null;
				synchronized(idSet) {
					toActivate = idSet.toArray(new Long[idSet.size()]);
				}
				if (toActivate != null) {
					ActivationManager.getInstance().activateAll(
							new ActivatorChunkUnloaded(chunk), toActivate);
				}
			}
		}
	} // end of internal class
}

