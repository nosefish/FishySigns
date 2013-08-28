package net.gmx.nosefish.fishysigns.plugin.engine;

import java.util.Arrays;

import net.canarymod.api.world.blocks.Sign;

import net.gmx.nosefish.fishylib.blocks.BlockInfo;
import net.gmx.nosefish.fishylib.worldmath.FishyDirection;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;

public class UnloadedSign {
	private final FishyLocationInt location;
	private final FishyDirection facingDirection;
	private final short blockType;
	private final String[] text;
	
	public UnloadedSign(Sign sign) {
		this(new FishyLocationInt(sign.getBlock().getLocation()),
		     BlockInfo.getSignDirection(sign.getBlock().getTypeId(), sign.getBlock().getData()),
		     sign.getBlock().getTypeId(),
		     sign.getText());
	}
	
	public UnloadedSign(FishyLocationInt location,
			               FishyDirection direction,
			                        short blockType,
			                     String[] text) {
		this.location = location;
		this.facingDirection = direction;
		this.blockType = blockType;
		this.text = Arrays.copyOf(text, text.length);
	}

	/**
	 * @return the location
	 */
	public FishyLocationInt getLocation() {
		return location;
	}

	/**
	 * @return the facingDirection
	 */
	public FishyDirection getFacingDirection() {
		return facingDirection;
	}

	/**
	 * @return the blockType
	 */
	public short getBlockType() {
		return blockType;
	}

	/**
	 * @return the text
	 */
	public String[] getText() {
		return Arrays.copyOf(text, text.length);
	}
}
