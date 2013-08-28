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
	 * Makes the caller block until the server ticks.
	 * DO NOT CALL FROM THE SERVER THREAD!
	 * 
	 * @throws DisabledException if the <code>ServerTicker</code> is not running
	 */
	public void awaitTick() throws DisabledException{
		if (shutdown) {
			throw new DisabledException("FishyLib has been disabled, the ServerTicker was shut down.");
		}
		long lastTick = tickCount;
		try {
			synchronized(instance) {
				while (tickCount == lastTick) {
					instance.wait();
					if (shutdown) {
						// notifyAll might have been called by shutdown() instead of tick().
						throw new DisabledException("FishySigns has been disabled and the ServerTicker was shut down.");
					}
				}
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}

	/**
	 * Gets the current tick count
	 * 
	 * @return the number of ticks since the <code>ServerTicker</code> was first enabled
	 * @throws DisabledException if the <code>ServerTicker</code> is not running
	 */
	public long getTickCount() throws DisabledException {
		synchronized(instance) {
			if (shutdown) {
				throw new DisabledException("FishySigns has been disabled and the ServerTicker was shut down.");
			}
			return tickCount;
		}
	}
	
	/**
	 * Shuts down the <code>ServerTicker</code>.
	 * Called by the plugin, do not call yourself!
	 */
	public void shutdown() {
		this.shutdown = true;
		synchronized(instance) {
			instance.notifyAll();
		}
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
		synchronized (instance) {
			++tickCount;
			instance.notifyAll();
		}
	}
	

}
