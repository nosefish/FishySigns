package net.gmx.nosefish.fishysigns.plugin.engine;


import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import net.gmx.nosefish.fishylib.worldmath.FishyChunk;
import net.canarymod.api.world.Chunk;
import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.TileEntity;
import net.canarymod.api.world.blocks.Sign;
import net.gmx.nosefish.fishysigns.Log;
import net.gmx.nosefish.fishysigns.task.FishyTask;

public class FishySignFinderTask extends FishyTask{
	private final Set<FishyChunk> chunks;
	
	public FishySignFinderTask(Set<FishyChunk> chunks) {
		this.chunks = chunks;
	}


	@Override
	public void doStuff() {
		List<UnloadedSign> signsToLoad= new LinkedList<UnloadedSign>();
		for (FishyChunk fishyChunk : chunks) {
			World world = fishyChunk.getWorld().getWorldIfLoaded();
			//TODO: bug workaround: world.getChunk needs world coordinates instead of chunk coordinates. Will probably break.
			Chunk chunk = world.getChunk(fishyChunk.getIntX(), fishyChunk.getIntZ());
			if (chunk != null) {
				for (TileEntity tileEntity : chunk.getTileEntityMap().values()) {
					if (tileEntity instanceof Sign) {
						signsToLoad.add(new UnloadedSign((Sign)tileEntity));
						System.out.println("FishySignFinder added sign to load");
					}
				}
			} else {
				Log.get().logWarning(this.getClass().getName() + " - Chunk not loaded: " + fishyChunk);
			}
		}
		if (! signsToLoad.isEmpty()) {
			this.setNextTask(new FishySignLoaderTask(signsToLoad));
		}
	}
	
}
