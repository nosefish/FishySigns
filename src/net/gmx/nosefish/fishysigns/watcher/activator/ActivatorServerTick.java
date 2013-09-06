package net.gmx.nosefish.fishysigns.watcher.activator;

public class ActivatorServerTick implements IActivator {
	private final int tick;
	
	public ActivatorServerTick(int tick) {
		this.tick = tick;
	}
	
	public int getTick() {
		return tick;
	}
}
