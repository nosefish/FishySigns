package net.gmx.nosefish.fishysigns.task.common;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.gmx.nosefish.fishylib.blocks.BlockInfo;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.Log;
import net.gmx.nosefish.fishysigns.exception.DisabledException;
import net.gmx.nosefish.fishysigns.plugin.engine.ServerTicker;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.task.TickDelayQueue;
import net.gmx.nosefish.fishysigns.task.TickDelayed;

/**
 * Provides a way to set a large number of levers from different FishySigns without
 * creating a separate FishyTask for each update.
 * 
 * @author Stefan Steinheimer
 * @deprecated I don't think this provides much of a performance advantage over simply using a FishyTask
 *     for each update 
 *
 */
@Deprecated
public class OutputLever {
	private static volatile LeverSwitchTask leverSwitchTask;
	private static volatile LeverSwitchTask delayedLeverSwitchTask;

	/**
	 * Sets the lever on the next tick
	 * 
	 * @param location
	 * @param state
	 */
	public static void set(FishyLocationInt location, boolean state){
		if (location == null){
			return;
		}
		LeverUpdate update = new LeverUpdate(location, state, 0);
		if (leverSwitchTask == null || leverSwitchTask.isCancelled()) {
			makeNewLeverSwitchTask();
		}
		leverSwitchTask.enQ(update);
	}
	
	/**
	 * Sets the lever on the target tick, or on the next tick
	 * if the target tick has already passed.
	 * 
	 * @param location
	 * @param state
	 * @param targetTick
	 */
	public static void setOnTick(FishyLocationInt location, boolean state, long targetTick){
		if (location == null){
			return;
		}
		LeverUpdate update = new LeverUpdate(location, state, targetTick);
		if (delayedLeverSwitchTask == null || delayedLeverSwitchTask.isCancelled()) {
			makeNewDelayedLeverSwitchTask();
		}
		delayedLeverSwitchTask.enQ(update);
	}
	
	private static synchronized void makeNewLeverSwitchTask() {
		if (leverSwitchTask == null || leverSwitchTask.isCancelled()) {
			leverSwitchTask = new LeverSwitchTask(new ConcurrentLinkedQueue<OutputLever.LeverUpdate>());
			leverSwitchTask.submit();
		}
	}
	
	private static synchronized void makeNewDelayedLeverSwitchTask() {
		if (delayedLeverSwitchTask == null || delayedLeverSwitchTask.isCancelled()) {
			delayedLeverSwitchTask = new LeverSwitchTask(new TickDelayQueue<OutputLever.LeverUpdate>());
			delayedLeverSwitchTask.submit();
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
		protected final Queue<LeverUpdate> q;
		private int idleCounter = 0;
		
		public LeverSwitchTask(Queue<LeverUpdate> queue) {
			this.q = queue;
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
				if (update.on != isOn) {
					// switch lever
					block.rightClick(null);
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
	private static class LeverUpdate implements TickDelayed {
		public final FishyLocationInt location;
		public final boolean on;
		public final long onTick;
		
		public LeverUpdate(FishyLocationInt location, boolean state, long targetTick) {
			this.location = location;
			this.on = state;
			this.onTick = targetTick;
		}

		@Override
		public long getTickDelay() {
			long delay = 0;
			try {
				delay = onTick - ServerTicker.getInstance().getTickCount();
			} catch (DisabledException e) {
				Log.get().trace(e);
			}
			return delay;
		}
	}
}
