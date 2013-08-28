package net.gmx.nosefish.fishysigns.activator;

import net.canarymod.api.world.blocks.Block;
import net.gmx.nosefish.fishysigns.world.ImmutableBlockState;
import net.gmx.nosefish.fishysigns.world.ImmutableLocationBlockState;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;

public class ImmutableRedstoneChange {
	ImmutableLocationBlockState blockState;
	int oldLevel;
	int newLevel;
	
	public ImmutableRedstoneChange(Block block, int oldLevel, int newLevel) {
		this.blockState = new ImmutableLocationBlockState(block);
		this.oldLevel = oldLevel;
		this.newLevel = newLevel;
	}
	
	public FishyLocationInt getLocation() {
		return blockState.getLocation();
	}
	
	public ImmutableBlockState getBlockState() {
		return blockState.getState();
	}
	
	public int getOldLevel() {
		return oldLevel;
	}
	
	public int getNewLevel() {
		return newLevel;
	}
	
	public boolean isDigitalChange() {
		return (oldLevel == 0) != (newLevel == 0);
	}
	
	public boolean isLowToHigh() {
		return (oldLevel == 0) && (newLevel > 0);
	}
	
	public boolean isHighToLow() {
		return (oldLevel > 0) && (newLevel == 0);
	}
	
	/**
	 * 
	 * @return
	 *     <code>true</code> for a rising flank,
	 *     <code>false</code> for a falling flank,
	 *     <code>null</code> if there was no high/low transition 
	 */
	public Boolean getDigitalChange() {
		if (isLowToHigh()) return true;
		if (isHighToLow()) return false;
		return null;
	}
}
