package net.gmx.nosefish.fishysigns.task.common;

import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.gmx.nosefish.fishylib.worldmath.FishyChunk;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.task.FishyTask;

public class SetBlockTask extends FishyTask {
	private final short id;
	private final short data;
	private final FishyLocationInt[] locations;
	private final boolean force;
	
	public SetBlockTask(short id, short data, boolean force, FishyLocationInt... locations) {
		this.id = id;
		this.data = data;
		this.locations = locations;
		this.force = force;
	}

	@Override
	public void doStuff() {
		for (FishyLocationInt loc : locations) {
			World world = loc.getWorld().getWorldIfLoaded();
			if (world == null) {
				continue;
			}
			if (! world.isChunkLoaded(
					FishyChunk.worldToChunk(loc.getIntX()), 
					FishyChunk.worldToChunk(loc.getIntZ()))) {
				return;
			}
			if (! force) {
				Block block = world.getBlockAt(loc.getIntX(), loc.getIntY(), loc.getIntZ());
				// TODO: this might need a more sophisticated check, other types might be OK, too
				if (block == null || block.getTypeId() != BlockType.Air.getId()) {
					continue;
				}
			}
			world.setBlockAt(loc.getIntX(), loc.getIntY(), loc.getIntZ(), id, data);
		}
	}
}
