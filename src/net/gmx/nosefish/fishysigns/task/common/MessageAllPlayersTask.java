package net.gmx.nosefish.fishysigns.task.common;

import net.canarymod.Canary;
import net.canarymod.chat.TextFormat;
import net.gmx.nosefish.fishysigns.task.FishyTask;

public class MessageAllPlayersTask extends FishyTask {
	private static final String DEFAULT_COLOUR = TextFormat.ORANGE;
	private final String colouredMessage;
	
	public MessageAllPlayersTask(String message) {
		this(message, DEFAULT_COLOUR);
	}
	
	public MessageAllPlayersTask(String message, String colour) {
		this.colouredMessage = colour + message;
	}
	
	@Override
	public void doStuff() {
		Canary.getServer().broadcastMessage(colouredMessage);
	}
}
