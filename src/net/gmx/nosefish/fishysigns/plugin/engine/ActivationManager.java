package net.gmx.nosefish.fishysigns.plugin.engine;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.gmx.nosefish.fishysigns.plugin.FishySigns;
import net.gmx.nosefish.fishysigns.watcher.IFishyWatcher;
import net.gmx.nosefish.fishysigns.watcher.activator.IActivatable;
import net.gmx.nosefish.fishysigns.watcher.activator.IActivator;

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
public final class ActivationManager implements IFishyWatcher{
	// singleton
	private static final ActivationManager instance = new ActivationManager();
	static{
		FishySigns.addWatcher(instance);
	}

	private volatile boolean enabled = false;
	// instance
	private final ReentrantReadWriteLock indexLock = new ReentrantReadWriteLock();
	private final Lock indexReadLock = indexLock.readLock();
	private final Lock indexWriteLock = indexLock.writeLock();
	private long idCounter; // guarded by indexLock
	private Map<Long, IActivatable> idIndex; // guarded by indexLock
	
	/**
	 * Private constructor for singleton
	 */
	private ActivationManager() {
		this.idCounter = 0L;
		this.idIndex = new HashMap<Long, IActivatable>(64, 0.8F);
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
	public void register(IActivatable activatable) {
		if (! enabled) {
			return;
		}
		long id;
		try {
			indexWriteLock.lock();
			id = ++idCounter;
			idIndex.put(id, activatable);
			activatable.setID(id);
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
	public boolean hasID(long id) {
		if (! enabled) {
			return false;
		}
		boolean result = false; 
		indexReadLock.lock();
		try {
			result = idIndex.containsKey(id);
		} finally {
			indexReadLock.unlock();
		}
		return result;
	}
	

	
		public void remove(Long id) {
			if (! enabled) {
				return;
			}
			IActivatable toRemove = null;
			indexWriteLock.lock();
			try {
				toRemove = idIndex.get(id);
				idIndex.remove(id);
			} finally {
				indexWriteLock.unlock();
			}
			if (toRemove != null) {
				toRemove.remove();
			}
		}
	
	/**
	 * Activates all registered <code>Activatables</code> represented by the ids in the list
	 * with the same <code>Activator</code>.
	 * 
	 * @param activator the <code>Activator</code> to send to the <code>Activatables</code>
	 * @param toActivate ids to activate
	 */
	public void activateAll(IActivator activator, Long... toActivate) {
		if (! enabled) {
			return;
		}
		for (Long id : toActivate) {
			activate(id, activator);
		}
	}
	
	
	/**
	 * Activates all registered <code>Activatables</code> represented by the ids in the list
	 * with the associated <code>Activator</code>.
	 * 
	 * @param toActivate id/<code>Activator</code> pairs to process
	 */
	public void activateAll(Map<Long, ? extends IActivator> toActivate) {
		if (! enabled) {
			return;
		}
		for (Map.Entry<Long, ? extends IActivator> entry : toActivate.entrySet()) {
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
	public void activate(long id, IActivator activator) {
		if (! enabled) {
			return;
		}
		IActivatable toActivate = null;
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

	@Override
	public void enable() {
		enabled = true;
	}

	@Override
	public void disable() {
		enabled = false;
		indexWriteLock.lock();
		try {
			idCounter = 0L;
			idIndex.clear();
		} finally {
			indexWriteLock.unlock();
		}
	}
}
