package net.gmx.nosefish.fishysigns.watcher.activator;

public class ActivatorServerTick implements IActivator {
	private final long tick;
	
	public ActivatorServerTick(long tick) {
		this.tick = tick;
	}
	
	public long getTick() {
		return tick;
	}
}
