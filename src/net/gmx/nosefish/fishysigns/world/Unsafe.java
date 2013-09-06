package net.gmx.nosefish.fishysigns.world;

import net.canarymod.api.world.blocks.Block;
import net.gmx.nosefish.fishylib.blocks.BlockInfo;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.watcher.ChunkTracker;

/**
 * Collection of methods for world access that will work most of the time
 * even when not called from the server thread. May return inconsistent
 * or slightly stale values (not older than 1 tick),
 * but will not crash the server or cause corruption.
 *  
 * @author Stefan Steinheimer (nosefish)
 *
 */
public final class Unsafe {
	/**
	 * This is not really thread-safe and will in some cases return inconsistent values.
	 * Only use when you don't mind that.
	 * Will not cause crashes, returns null on exceptions. 
	 * Do not change the returned block!
	 * 
	 * @param blockLoc
	 * @return the block at the location (most of the time), or null if something goes wrong
	 */
	public static FishyBlockState unsafeGetBlockAt(FishyLocationInt blockLoc) {
		Block block = null;
		WorldValuePublisher.publish();
		try {
			if (ChunkTracker.getInstance().isChunkLoaded(blockLoc)) {
				block = blockLoc.getWorld().getWorldIfLoaded().getBlockAt(
						                                         blockLoc.getIntX(),
						                                         blockLoc.getIntY(),
						                                         blockLoc.getIntZ());
			}
		} catch (Exception e) {
			// hmmmmm... *looks left and right* ... has anyone seen that?
		}
		return new FishyBlockState(block);
	}
	
	
	public static Boolean unsafeGetDirectInput(FishyLocationInt inputBlock,
	                                                      short id,
	                                                      short data,
	                                           FishyLocationInt targetBlock) {
		Boolean result = null;
		WorldValuePublisher.publish();
		try {
			result = BlockInfo.isDirectInput(inputBlock, id, data, targetBlock);
		} catch(Exception e) {
			// oops..
		}
		return result;
	}
}
