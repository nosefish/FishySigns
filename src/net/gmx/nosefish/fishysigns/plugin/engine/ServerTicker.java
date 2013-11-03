package net.gmx.nosefish.fishysigns.plugin.engine;


import net.canarymod.plugin.PluginListener;
import net.gmx.nosefish.fishysigns.exception.DisabledException;

/**
 * @author Stefan Steinheimer (nosefish)
 *
 */
public class ServerTicker implements PluginListener {
	private static volatile ServerTicker instance = new ServerTicker();
	private static volatile long tickCount = 0L;
	private volatile boolean shutdown = true;
	
	private ServerTicker() {

	}

	public static ServerTicker getInstance() {
		return instance;
	}
	


	/**
	 * Gets the current tick count
	 * 
	 * @return the number of ticks since the <code>ServerTicker</code> was first enabled
	 * @throws DisabledException if the <code>ServerTicker</code> is not running
	 */
	public long getTickCount() throws DisabledException {
			if (shutdown) {
				throw new DisabledException("FishySigns has been disabled and the ServerTicker was shut down.");
			}
			return tickCount;
	}
	
	/**
	 * Shuts down the <code>ServerTicker</code>.
	 * Called by the plugin, do not call yourself!
	 */
	public void shutdown() {
		this.shutdown = true;
	}
	
	/**
	 * Starts the <code>ServerTicker</code>.
	 * Called by the plugin, do not call yourself!
	 */
	public void start() {
		this.shutdown = false;
	}
	
	/**
	 * Called by the FishyEngineListener when the server ticks. Do not call yourself!
	 */
	public void tick() {
		if (tickCount == Long.MAX_VALUE) {
			throw new OverflowException("The tick counter has reached Long.MAX_VALUE. Time to restart the server. How did you keep it up for such a long time?");
		}
		++tickCount;
	}
	
	public static class OverflowException extends RuntimeException {

		public OverflowException() {
			super();
		}

		public OverflowException(String message) {
			super(message);
		}

		private static final long serialVersionUID = -8510961058618323899L;
	}

}
