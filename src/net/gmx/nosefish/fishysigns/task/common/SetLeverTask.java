package net.gmx.nosefish.fishysigns.task.common;

import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.gmx.nosefish.fishylib.blocks.BlockInfo;
import net.gmx.nosefish.fishylib.worldmath.FishyChunk;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.Log;
import net.gmx.nosefish.fishysigns.exception.DisabledException;
import net.gmx.nosefish.fishysigns.plugin.engine.ServerTicker;
import net.gmx.nosefish.fishysigns.task.FishyTask;

public class SetLeverTask extends FishyTask {
	private FishyLocationInt location;
	private boolean newState;

	public SetLeverTask(FishyLocationInt location, boolean newState, long targetTick) {
		this.location = location;
		this.newState = newState;
		long delay = 0;
		try {
			delay = targetTick - ServerTicker.getInstance().getTickCount();
		} catch (DisabledException e) {
			// Let's see if this ever happens.
			Log.get().warn("SetLeverTask: the ServerTicker is disabled.");
		}
		this.setTickDelay(delay);
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
		Block block = world.getBlockAt(location.getIntX(),
		                               location.getIntY(),
		                               location.getIntZ());
		if (block.getTypeId() == BlockType.Lever.getId()) {
			boolean isOn = BlockInfo.getRedstonePower(block.getTypeId(), block.getData()) > 0;
			if (newState != isOn) {
				// switch lever
				block.rightClick(null);
			} 
		}
	}

}
