package net.gmx.nosefish.fishysigns.task.common;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.gmx.nosefish.fishylib.blocks.BlockInfo;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.task.FishyTask;

/**
 * Provides a way to set a large number of levers from different FishySigns without
 * creating a separate FishyTask for each update.
 * 
 * @author Stefan Steinheimer
 *
 */
public class OutputLever {
	private static volatile LeverSwitchTask leverSwitchTask;

	public static void set(FishyLocationInt location, boolean state){
		if (location == null){
			return;
		}
		LeverUpdate update = new LeverUpdate(location, state);
		if (leverSwitchTask == null || leverSwitchTask.isCancelled()) {
			makeNewLeverSwitchTask();
		}
		leverSwitchTask.enQ(update);
	}
	
	private static synchronized void makeNewLeverSwitchTask() {
		if (leverSwitchTask == null || leverSwitchTask.isCancelled()) {
			leverSwitchTask = new LeverSwitchTask();
			leverSwitchTask.submit();
		}
	}
	
	/**
	 * Runs every tick and updates levers in the world
	 * 
	 * @author Stefan Steinheimer (nosefish)
	 *
	 */
	private static class LeverSwitchTask extends FishyTask {
		private static final int IDLE_MAX = 200; // stop task after 200 ticks without updates (~10s)
		private static final int OR_LEVER_ON_BITS = 0x8;
		private static final int AND_LEVER_OFF_BITS = ~0x8;
		private final Queue<LeverUpdate> q = new ConcurrentLinkedQueue<LeverUpdate>();
		private int idleCounter = 0;
		
		public LeverSwitchTask() {
			this.setTickRepeatDelay(1);
		}
		
		public void enQ(LeverUpdate update) {
			q.offer(update);
		}
		
		@Override
		public void doStuff() {
			if (q.peek() == null) {
				idleCounter++;
				if (idleCounter > IDLE_MAX) {
					this.cancel();
				}
			}
			while(true) {
				LeverUpdate update = q.poll();
				if (update != null) {
					this.updateLever(update);
				}
				else {
					break;
				}
			}
			idleCounter = 0;
		}
		
		private void updateLever(LeverUpdate update) {
			World world = update.location.getWorld().getWorldIfLoaded();
			if (world == null) return;
			Block block = world.getBlockAt(update.location.getIntX(),
			                               update.location.getIntY(),
			                               update.location.getIntZ());
			if (block.getTypeId() == BlockType.Lever.getId()) {
				boolean isOn = BlockInfo.getRedstonePower(block.getTypeId(), block.getData()) > 0;
				if (update.on && !isOn) {
					// switch on
					block.setData((short)(block.getData() | OR_LEVER_ON_BITS));
					block.update();
				} else if (! update.on && isOn) {
					// switch off
					block.setData((short)(block.getData() & AND_LEVER_OFF_BITS));
					block.update();
				}
			}
		}
	}
	
	/**
	 * Something to enqueue
	 * 
	 * @author Stefan Steinheimer (nosefish)
	 *
	 */
	private static class LeverUpdate {
		public final FishyLocationInt location;
		public final boolean on;
		
		public LeverUpdate(FishyLocationInt location, boolean state) {
			this.location = location;
			this.on = state;		
		}
	}
}
