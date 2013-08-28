package net.gmx.nosefish.fishysigns.world;

import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.canarymod.api.world.blocks.Block;

public final class ImmutableLocationBlockState {
	private final FishyLocationInt location;
	private final ImmutableBlockState state;

	public ImmutableLocationBlockState(Block block) {
		this.location = new FishyLocationInt(block.getLocation());
		this.state = new ImmutableBlockState(block);
	}
	
	public ImmutableLocationBlockState(FishyLocationInt location, short typeId, short data) {
		this.location = location;
		this.state = new ImmutableBlockState(typeId, data);
	}
	
	/**
	 * 
	 * @return the location
	 */
	public FishyLocationInt getLocation() {
		return location;
	}
	
	
	public ImmutableBlockState getState() {
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
		if (other instanceof ImmutableLocationBlockState) {
			ImmutableLocationBlockState otherState = (ImmutableLocationBlockState) other;
			return this.location.equals(otherState.location)
					&& this.state.equals(otherState.state);
		}
		return false;
	}
}
