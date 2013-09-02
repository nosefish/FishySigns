package net.gmx.nosefish.fishysigns.activator;

public class ActivatorServerTick implements Activator {
	private final int tick;
	
	public ActivatorServerTick(int tick) {
		this.tick = tick;
	}
	
	public int getTick() {
		return tick;
	}
}
