package net.gmx.nosefish.fishysigns.activator;

import net.gmx.nosefish.fishysigns.world.ImmutableBlockState;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;

public final class ImmutableBlockStateChange {
	private final FishyLocationInt location;
	private final ImmutableBlockState oldState;
	private final ImmutableBlockState newState;
	
	
	public ImmutableBlockStateChange(FishyLocationInt location, ImmutableBlockState oldState, ImmutableBlockState newState) {
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
	public ImmutableBlockState getOldState() {
		return oldState;
	}


	/**
	 * @return the newState
	 */
	public ImmutableBlockState getNewState() {
		return newState;
	}

	
}
