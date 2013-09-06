package net.gmx.nosefish.fishysigns.watcher.activator;

import net.gmx.nosefish.fishysigns.world.FishyBlockState;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;

public final class FishyBlockStateChange {
	private final FishyLocationInt location;
	private final FishyBlockState oldState;
	private final FishyBlockState newState;
	
	
	public FishyBlockStateChange(FishyLocationInt location, FishyBlockState oldState, FishyBlockState newState) {
		this.location = location;
		this.oldState = oldState;
		this.newState = newState;
	}
	
	/**
	 * @return the location
	 */
	public FishyLocationInt getLocation() {
		return location;
	}


	/**
	 * @return the oldState
	 */
	public FishyBlockState getOldState() {
		return oldState;
	}


	/**
	 * @return the newState
	 */
	public FishyBlockState getNewState() {
		return newState;
	}

	
}
