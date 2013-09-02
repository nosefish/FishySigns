package net.gmx.nosefish.fishysigns.plugin.engine;


import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import net.gmx.nosefish.fishylib.worldmath.FishyChunk;
import net.canarymod.api.world.Chunk;
import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.TileEntity;
import net.canarymod.api.world.blocks.Sign;
//import net.gmx.nosefish.fishysigns.Log;
import net.gmx.nosefish.fishysigns.task.FishyTask;

public class FishySignFinderTask extends FishyTask{
	private final Collection<FishyChunk> chunks;
	
	public FishySignFinderTask(Collection<FishyChunk> chunks) {
		this.chunks = chunks;
	}

	@Override
	public void doStuff() {
		List<UnloadedSign> signsToLoad= new LinkedList<UnloadedSign>();
		for (FishyChunk fishyChunk : chunks) {
			World world = fishyChunk.getWorld().getWorldIfLoaded();
			Chunk chunk = world.getChunk(fishyChunk.getChunkX(), fishyChunk.getChunkZ());
			if (chunk != null) {
				for (TileEntity tileEntity : chunk.getTileEntityMap().values()) {
					if (tileEntity instanceof Sign) {
						signsToLoad.add(new UnloadedSign((Sign)tileEntity));
					}
				}
			} else {
				//Log.get().logWarning(this.getClass().getName() + " - Chunk not loaded: " + fishyChunk);
			}
		}
		if (! signsToLoad.isEmpty()) {
			this.setNextTask(new FishySignLoaderTask(signsToLoad));
		}
	}
	
}
