package net.gmx.nosefish.fishysigns.world;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.gmx.nosefish.fishylib.worldmath.FishyChunk;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishylib.worldmath.FishyVectorInt;

import net.canarymod.api.world.Chunk;

/**
 * Provides a thread-safe way to determine whether a chunk or a chunk cluster is loaded.
 * A chunk cluster means a chunk and the 8 chunks surrounding it.
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 */
public class ChunkTracker {
	private static ChunkTracker instance = new ChunkTracker();
	private Map<FishyChunk, Boolean> loadedChunks;
	
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
	//TODO: multi-threaded chunk loading/unloading will probably break add and remove when it's added to Canary
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
	 * Called by the FishyEngineListener. Do not call from anywhere else.
	 * 
	 * @param chunk the chunk to add.
	 * @return the chunks in the cluster that are now in a completely loaded cluster 
	 */
	public Set<FishyChunk> add(Chunk chunk) {
		Set<FishyChunk> loadedClusters = new HashSet<FishyChunk>(10, 0.9F);
		FishyChunk newChunk = new FishyChunk(chunk);
		boolean neighboursLoaded = areAllNeighbouringChunksLoaded(newChunk);
		loadedChunks.put(newChunk, neighboursLoaded);
		//TODO: more efficient implementation
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
	public Set<FishyChunk> remove(Chunk chunk) {
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
	
	/**
	 * Removes all entries from the tracker
	 * Called by the Plugin. Do not call from anywhere else.
	 * 
	 */
	public void clear() {
		loadedChunks.clear();
	}
	
	public static ChunkTracker getInstance() {
		return instance;
	}
}

