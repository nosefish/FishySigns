package net.gmx.nosefish.fishysigns.watcher.activator;

import net.gmx.nosefish.fishylib.worldmath.FishyChunk;

public class ActivatorChunkUnloaded implements IActivator {
	private final FishyChunk chunk;
	
	public ActivatorChunkUnloaded(FishyChunk chunk) {
		this.chunk = chunk;
	}
	
	public FishyChunk getChunk() {
		return chunk;
	}
}
