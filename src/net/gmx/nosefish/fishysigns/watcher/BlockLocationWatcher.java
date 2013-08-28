package net.gmx.nosefish.fishysigns.watcher;

import net.gmx.nosefish.fishysigns.activator.Activatable;
import net.gmx.nosefish.fishylib.datastructures.ConcurrentMapWithTreeSet;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;

public abstract class BlockLocationWatcher {
	/**
	 * The set is a <code>Collections.synchronizedSet</code>.
	 * When iterating over it, you must synchronize on it!
	 */
	protected final ConcurrentMapWithTreeSet<FishyLocationInt, Long> blockLocationIndex =
			new ConcurrentMapWithTreeSet<FishyLocationInt, Long>();

	/**
	 * Registers an <code>Activatable</code> with a block location
	 * to be handled by the watcher.
	 * 
	 * @param activatable
	 * @param location
	 */
	public void register(Activatable activatable, FishyLocationInt location) {
		blockLocationIndex.put(location, activatable.getID());
	}
	
	
	/**
	 * Removes a registered <code>Activatable</code> from
	 * this watcher.
	 * 
	 * @param activatable
	 * @param location
	 */
	public void remove(Activatable activatable) {
		blockLocationIndex.removeValue(activatable.getID());
	}

}
