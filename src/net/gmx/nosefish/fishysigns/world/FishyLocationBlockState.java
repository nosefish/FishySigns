package net.gmx.nosefish.fishysigns.world;

import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.canarymod.api.world.blocks.Block;

public final class FishyLocationBlockState {
	private final FishyLocationInt location;
	private final FishyBlockState state;

	public FishyLocationBlockState(Block block) {
		this.location = new FishyLocationInt(block.getLocation());
		this.state = new FishyBlockState(block);
	}
	
	public FishyLocationBlockState(FishyLocationInt location, short typeId, short data) {
		this.location = location;
		this.state = new FishyBlockState(typeId, data);
	}
	
	/**
	 * 
	 * @return the location
	 */
	public FishyLocationInt getLocation() {
		return location;
	}
	
	
	public FishyBlockState getState() {
		return state;
	}
	
	/**
	 * @return the typeId
	 */
	public short getTypeId() {
		return state.getTypeId();
	}

	/**
	 * @return the data
	 */
	public short getData() {
		return state.getData();
	}
	
	@Override
	public boolean equals(Object other) {
		if (other instanceof FishyLocationBlockState) {
			FishyLocationBlockState otherState = (FishyLocationBlockState) other;
			return this.location.equals(otherState.location)
					&& this.state.equals(otherState.state);
		}
		return false;
	}
}
