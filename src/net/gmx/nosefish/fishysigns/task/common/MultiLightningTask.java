package net.gmx.nosefish.fishysigns.task.common;

import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.task.FishyTask;

public class MultiLightningTask extends FishyTask {
	private final FishyLocationInt[] targets;
	private final double chance;
	
	public MultiLightningTask(FishyLocationInt[] targets, double chance) {
		this.targets = targets;
		this.chance = chance;
		if (chance <= 0.0 || targets == null || targets.length == 0) {
			this.cancel();
		}
	}

	@Override
	public void doStuff() {
		for (FishyLocationInt oneTarget : targets) {
			if (chance == 1.0 || Math.random() <= chance) {
				LightningTask lightningTask = new LightningTask(oneTarget);
				lightningTask.submit();
			}
		}
	}

}
