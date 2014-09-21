package net.gmx.nosefish.fishysigns.world;

import net.canarymod.api.world.blocks.Block;

public class FishyBlockState {
	private final short typeId;
	private final short data;
	
	public FishyBlockState(Block block) {
		this.typeId = block.getTypeId();
		this.data = block.getData();
	}
	
	public FishyBlockState(short blockId, short data) {
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

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + this.typeId;
        hash = 29 * hash + this.data;
        return hash;
    }
	
    @Override
	public boolean equals(Object other) {
		if (other instanceof FishyBlockState) {
			FishyBlockState otherState = (FishyBlockState) other;
			return this.typeId == otherState.typeId
					&& this.data == otherState.data;
		}
		return false;
	}
}
