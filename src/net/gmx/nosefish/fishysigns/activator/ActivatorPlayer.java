package net.gmx.nosefish.fishysigns.activator;

import net.gmx.nosefish.fishysigns.world.ImmutableLocationBlockState;

public class ActivatorPlayer implements Activator {
	private final String playerName;
	private final ImmutableLocationBlockState block;
	
	public ActivatorPlayer(String name, ImmutableLocationBlockState block) {
		this.playerName = name;
		this.block = block;
	}
	
	public String getPlayerName() {
		return playerName;
	}
	
	public ImmutableLocationBlockState getBlockState() {
		return block;
	}
}
