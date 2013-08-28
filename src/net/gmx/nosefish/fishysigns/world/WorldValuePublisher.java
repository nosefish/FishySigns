package net.gmx.nosefish.fishysigns.world;

import net.gmx.nosefish.fishysigns.exception.DisabledException;
import net.gmx.nosefish.fishysigns.plugin.engine.ServerTicker;

/**
 * Before reading from the world outside the main thread, call the
 * <code>publish</code> method to make sure that the values you're 
 * getting are not older than one tick. <b>This does not mean you'll
 * get <i>consistent</i> values!</b>
 * 
 * @author Stefan Steinheimer
 *
 */
public final class WorldValuePublisher {
	
	/**
	 * Reads a volatile variable that is written by the main thread every tick.
	 * The java memory model guarantees that all changes performed
	 * before the last write will be visible to the calling thread.
	 */
	public static void publish() {
		try {
			@SuppressWarnings("unused")
			long memoryBarrier = ServerTicker.getInstance().getTickCount();
		} catch (DisabledException e) {
			// if it's disabled, the world values we're going to read won't matter anyway
		}
	}
}
