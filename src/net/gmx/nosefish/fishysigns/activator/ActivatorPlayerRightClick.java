package net.gmx.nosefish.fishysigns.activator;

import net.gmx.nosefish.fishysigns.world.ImmutableLocationBlockState;

/**
 * Contains information about a right mouse button click
 * performed by a player.
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 */
public class ActivatorPlayerRightClick implements Activator {
	private final String playerName;
	private final ImmutableLocationBlockState block;
	
	/**
	 * Constructor
	 * 
	 * @param 
	 *     name the name of the clicking player
	 *     
	 * @param block
	 *     the block on which the player clicked
	 */
	public ActivatorPlayerRightClick(String name, ImmutableLocationBlockState block) {
		this.playerName = name;
		this.block = block;
	}
	
	/**
	 * Gets the name of the clicking player.
	 * 
	 * @return
	 *     the player name
	 */
	public String getPlayerName() {
		return playerName;
	}
	
	/**
	 * Gets a thread-safe representation of the block on which the player clicked.
	 * 
	 * @return
	 *    the block
	 */
	public ImmutableLocationBlockState getBlockState() {
		return block;
	}
}
