package net.gmx.nosefish.fishysigns.task.common;

import net.canarymod.api.world.World;
import net.gmx.nosefish.fishylib.worldmath.FishyChunk;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.task.FishyTask;

/**
 * Strikes the specified location with lightning
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 */
public class LightningTask extends FishyTask {
	protected FishyLocationInt location;
	
	public LightningTask(FishyLocationInt location) {
		this.location = location;
	}

	@Override
	public void doStuff() {
		World world = location.getWorld().getWorldIfLoaded();
		if (world == null) {
			return;
		}
		if (! world.isChunkLoaded(
				FishyChunk.worldToChunk(location.getIntX()), 
				FishyChunk.worldToChunk(location.getIntZ()))) {
			return;
		}
		world.makeLightningBolt(location.getIntX(), location.getIntY(), location.getIntZ());
	}
}
