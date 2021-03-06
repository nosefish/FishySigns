package net.gmx.nosefish.fishysigns.watcher.activator;

import net.canarymod.api.world.blocks.Block;
import net.gmx.nosefish.fishysigns.world.FishyBlockState;
import net.gmx.nosefish.fishysigns.world.FishyLocationBlockState;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;

public class FishyRedstoneChange {
	protected final FishyLocationBlockState blockState;
	protected final int oldLevel;
	protected final int newLevel;
	protected final long tickStamp;
	
	public FishyRedstoneChange(Block block, int oldLevel, int newLevel, long tickStamp) {
		this.blockState = new FishyLocationBlockState(block);
		this.oldLevel = oldLevel;
		this.newLevel = newLevel;
		this.tickStamp = tickStamp;
	}
	
	public FishyLocationInt getLocation() {
		return blockState.getLocation();
	}
	
	public FishyBlockState getBlockState() {
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
	
	public long getTick() {
		return tickStamp;
	}
}
