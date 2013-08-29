package net.gmx.nosefish.fishysigns.task.common;

import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.chat.Colors;
import net.gmx.nosefish.fishysigns.task.FishyTask;

public class MessagePlayerTask extends FishyTask {
	private static final String DEFAULT_COLOUR = Colors.ORANGE;
	private final String playerName;
	private final String colouredMessage;
	
	public MessagePlayerTask(String playerName, String message) {
		this(playerName, message, DEFAULT_COLOUR);
	}
	
	public MessagePlayerTask(String playerName, String message, String colour) {
		this.playerName = playerName;
		this.colouredMessage = colour + message;
	}
	
	@Override
	public void doStuff() {
		Player player = Canary.getServer().getPlayer(playerName);
		if (player != null) {
			player.message(colouredMessage);
		}
	}
}
