package net.gmx.nosefish.fishysigns.plugin.engine;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.gmx.nosefish.fishysigns.activator.Activatable;
import net.gmx.nosefish.fishysigns.activator.Activator;
import net.gmx.nosefish.fishysigns.task.FishyTask;

import net.gmx.nosefish.fishylib.datastructures.ConcurrentMapWithTreeSet;
import net.gmx.nosefish.fishylib.worldmath.FishyChunk;
import net.gmx.nosefish.fishylib.worldmath.FishyLocation;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;

/**
 * Holds an index of all known <code>Activatables</code>. Only <code>Acivatable</code>s
 * registered here can be activated by the watchers. Usually, registration is
 * automatically performed immediately after instantiation in a factory method.
 * <p> 
 * No other class should retain a strong reference to an <code>Activatable</code>.
 * They should always be represented by their unique IDs instead.
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 */
public final class ActivationManager {
	// singleton
	private static final ActivationManager instance = new ActivationManager();

	// instance
	private final ReentrantReadWriteLock indexLock = new ReentrantReadWriteLock();
	private final Lock indexReadLock = indexLock.readLock();
	private final Lock indexWriteLock = indexLock.writeLock();
	private long idCounter; // guarded by indexLock
	private Map<Long, Activatable> idIndex; // guarded by indexLock
	private Map<FishyLocation, Long> locationIndex; // guarded by indexLock
	private ConcurrentMapWithTreeSet<FishyLocation, Long> chunkIndex; // guarded by indexLock
	
	/**
	 * Private constructor for singleton
	 */
	private ActivationManager() {
		this.idCounter = 0;
		this.idIndex = new HashMap<Long, Activatable>(64, 0.8F);
		this.locationIndex = new HashMap<FishyLocation, Long>(64, 0.8F);
		this.chunkIndex = new ConcurrentMapWithTreeSet<FishyLocation, Long>();
	}
	
	/**
	 * FishySignManager is a singleton. Use this method to access it.
	 * @return the FishySignManager instance
	 */
	public static ActivationManager getInstance() {
		return instance;
	}
	
	/**
	 * Registers an <code>Activatable</code>. Only registered
	 * <code>Activatable</code>s can be activated by watchers.
	 * <p>
	 * When the <code>Activatable</code> has been registered, 
	 * its <code>initialize</code> method is called.
	 * 
	 * @param activatable the <code>Activatable</code> to register
	 */
	public void register(Activatable activatable) {
		long id;
		try {
			indexWriteLock.lock();
			id = ++idCounter;
			idIndex.put(id, activatable);
			if (activatable.getLocation() != null) {
				locationIndex.put(activatable.getLocation(), id);
				chunkIndex.put(FishyChunk.getChunkContaining(activatable.getLocation()), id);
				activatable.setID(id);
			}
		} finally {
			indexWriteLock.unlock();
		}
		activatable.initialize();
	}
	
	/**
	 * Checks if the id is registered.
	 * 
	 * @param id
	 * @return
	 * 		<code>true</code> if the ActivationManager has an entry for the id, <code>false</code otherwise.
	 */
	public boolean hasID(long id){
		boolean result = false; 
		indexReadLock.lock();
		try {
			result = idIndex.containsKey(id);
		} finally {
			indexReadLock.unlock();
		}
		return result;
	}
	
	/**
	 * Called by the <code>FishyEngineListener</code> when a chunk is unloaded.
	 * 
	 * @param chunk
	 */
	public void removeAllInChunk(FishyChunk chunk) {
		RemoveAllActivatablesInChunkTask task = new RemoveAllActivatablesInChunkTask(chunk);
		task.submit();
	}
	
	/**
	 * Called by the <code>FishyEngineListener</code> when a block is destroyed,
	 * and by the <code>RemoveAllActivatablesInChunkTask</code>.
	 * 
	 * @param location
	 */
	public void remove(FishyLocation location) {
		Long id;
		Activatable toRemove = null;
		indexWriteLock.lock();
		try {
			id = locationIndex.get(location);
			toRemove = idIndex.get(id);
			// remove id from indices
			if (id != null) {
				locationIndex.remove(location);
				chunkIndex.removeValue(id);
				idIndex.remove(id);

			}
		} finally {
			indexWriteLock.unlock();
		}
		if (toRemove != null) {
				toRemove.remove();
		}
	}
	
	/**
	 * Activates all registered <code>Activatables</code> represented by the ids in the list
	 * with the associated <code>Activator</code>.
	 * 
	 * @param toActivate id/<code>Activator</code> pairs to process
	 */
	public void activateAll(Map<Long, ? extends Activator> toActivate) {
		for (Map.Entry<Long, ? extends Activator> entry : toActivate.entrySet()) {
			activate(entry.getKey(), entry.getValue());
		}
	}
	

	/**
	 * Activates the <code>Activatable</code> represented by <code>id</code>
	 * with the <code>Activator</code>.
	 * 
	 * @param id
	 * @param activator
	 */
	public void activate(long id, Activator activator) {
		Activatable toActivate = null;
		indexReadLock.lock();
		try {
			toActivate = idIndex.get(id);
		}finally {
			indexReadLock.unlock();
		}
		
		if (toActivate != null) {
			toActivate.activate(activator);
		}
	}
	
	/**
	 * Activates the <code>Activatable</code> at the specified
	 * location with the <code>Activator</code>.
	 * 
	 * @param location
	 * @param activator
	 */
	public void activate(FishyLocationInt location, Activator activator) {
		long id;
		try {
			indexReadLock.lock();
			id = locationIndex.get(location);
		} finally {
			indexReadLock.unlock();
		}
		this.activate(id, activator);
	}
	
	/**
	 * An asynchronous <code>FishyTask</code> that removes all
	 * <code>Acivatable</code>s in the specified chunk from the indices.
	 * 
	 * @author Stefan Steinheimer
	 *
	 */
	private class RemoveAllActivatablesInChunkTask extends FishyTask {
		FishyChunk chunk;
		
		public RemoveAllActivatablesInChunkTask(FishyChunk chunk) {
			this.setThreadsafe_IPromiseThatThisDoesNotTouchTheWorld();
			this.chunk = chunk;
		}

		@Override
		public void doStuff() {
			Set<Long> idSet = null;
			List<FishyLocation> locationsInChunk = new LinkedList<FishyLocation>();
			List<Long> idsInChunk = new LinkedList<Long>();
			indexReadLock.lock();
			try {
				idSet = chunkIndex.get(chunk);
				if (idSet != null) {
					// there are registered Activatables in this chunk
					// let's find out where exactly
					synchronized(idSet) {
						for (long id : idSet) {
							idsInChunk.add(id);
							Activatable activatable = idIndex.get(id);
							locationsInChunk.add(activatable.getLocation());
						}
					}
				}
			} finally {
				indexReadLock.unlock();
			}
			for (FishyLocation location : locationsInChunk) {
				ActivationManager.this.remove(location);
			}
		}
	} // end of internal class

}
