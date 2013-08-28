package net.gmx.nosefish.fishysigns.world;

import net.canarymod.api.world.blocks.Block;

public class ImmutableBlockState {
	private final short typeId;
	private final short data;
	
	public ImmutableBlockState(Block block) {
		this.typeId = block.getTypeId();
		this.data = block.getData();
	}
	
	public ImmutableBlockState(short blockId, short data) {
		this.typeId = blockId;
		this.data = data;
	}
	
	/**
	 * @return the typeId
	 */
	public short getTypeId() {
		return typeId;
	}

	/**
	 * @return the data
	 */
	public short getData() {
		return data;
	}
	
	public boolean equalsBlock(Block block) {
		return this.typeId == block.getTypeId()
				&& this.data == block.getData();
	}
	
	public boolean equals(Object other) {
		if (other instanceof ImmutableBlockState) {
			ImmutableBlockState otherState = (ImmutableBlockState) other;
			return this.typeId == otherState.typeId
					&& this.data == otherState.data;
		}
		return false;
	}
}
