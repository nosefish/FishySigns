package net.gmx.nosefish.fishysigns.watcher;

import net.gmx.nosefish.fishylib.datastructures.ConcurrentMapWithSet;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;

public abstract class BlockLocationWatcher implements IFishyWatcher{
	protected boolean enabled = false;
	
	/**
	 * The set is a <code>Collections.synchronizedSet</code>.
	 * When iterating over it, you must synchronize on it!
	 */
	protected final ConcurrentMapWithSet<FishyLocationInt, Long> blockLocationIndex =
			new ConcurrentMapWithSet<>();

	/**
	 * Registers an <code>Activatable</code> with a block location
	 * to be handled by the watcher.
	 * 
     * @param activatableID
	 * @param location
	 */
	public void register(Long activatableID, FishyLocationInt location) {
		blockLocationIndex.put(location, activatableID);
	}
	
	
	/**
	 * Removes a registered <code>Activatable</code> from
	 * this watcher.
	 * 
     * @param activatableID
	 */
	public void remove(Long activatableID) {
		blockLocationIndex.removeValue(activatableID);
	}
	
	
	@Override
	public void enable() {
		this.blockLocationIndex.clear();
		enabled = true;
	}

	@Override
	public void disable() {
		enabled = false;
		this.blockLocationIndex.clear();
	}

}
