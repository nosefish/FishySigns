package net.gmx.nosefish.fishysigns;

import net.canarymod.logger.Logman;
import net.canarymod.plugin.Plugin;

/**
 * Static access to the plugin's logger. 
 *  
 * @author Stefan Steinheimer (nosefish)
 *
 */
// FishySigns aren't plugins, so they don't have their own.
// They don't even know which plugin loads them, so this
// is a simple way to provide them with logger access.
public final class Log {
	private static Logman logger = null;
	
	/**
	 * Initializes the class. Called by the plugin.
	 * 
	 * @param plugin
	 */
	public static void initialize(Plugin plugin) {
		logger = plugin.getLogman();
		// TODO: find out how to set log level properly
	}
	
	/**
	 * Gets the FishySigns plugin's <code>Logman</code>.
	 * 
	 * @return
	 *     the <code>Logman</code>
	 */
	public static Logman get() {
		if (logger != null) {
			return logger;
		} else {
			throw new NullPointerException("Logger.get called before initialize");
		}
	}
	
}
