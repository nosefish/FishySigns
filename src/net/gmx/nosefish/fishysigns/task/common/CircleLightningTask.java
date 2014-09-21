package net.gmx.nosefish.fishysigns.task.common;

import net.canarymod.api.world.World;
import net.gmx.nosefish.fishylib.worldmath.CircleRunner;
import net.gmx.nosefish.fishylib.worldmath.FishyChunk;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishylib.worldmath.CircleRunner.IRunnableXY;
import net.gmx.nosefish.fishysigns.task.FishyTask;

public class CircleLightningTask extends FishyTask implements IRunnableXY {
	private final FishyLocationInt target;
	private final double chance;
	private final int radius;
	private final boolean fill;
	
	public CircleLightningTask(FishyLocationInt target, int radius, double chance, boolean fill) {
		this.target = target;
		this.chance = chance;
		this.radius = radius;
		this.fill = fill;
		if (chance <= 0.0 || target == null) {
			this.cancel();
		}
	}

	@Override
	public void doStuff() {
		if (radius == 0) {
			if (chance == 1.0 || Math.random() <= chance) {
				World world = target.getWorld().getWorldIfLoaded();
				if (world == null) {
					return;
				}
				if (! world.isChunkLoaded(
						FishyChunk.worldToChunk(target.getIntX()), 
						FishyChunk.worldToChunk(target.getIntZ()))) {
					return;
				}
				world.makeLightningBolt(
						target.getIntX(),
						target.getIntY(),
						target.getIntZ());
				
			}
		} else {
			CircleRunner.runOnCircle(target.getIntX(), target.getIntZ(), radius, fill, this);
		}
	}
	
	@Override
	public void run(int x, int z) {
		World world = target.getWorld().getWorldIfLoaded();
		if (chance == 1.0 || Math.random() <= chance) {
			if (world != null) {
				world.makeLightningBolt(
						x,
						target.getIntY(),
						z);
			}
		}
	}

}
