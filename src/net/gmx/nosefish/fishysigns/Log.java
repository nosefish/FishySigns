package net.gmx.nosefish.fishysigns;

import net.canarymod.logger.CanaryLevel;
import net.canarymod.logger.Logman;
import net.canarymod.plugin.Plugin;

public final class Log {
	private static Logman logger = null;
	
	public static void initialize(Plugin plugin) {
		logger = plugin.getLogman();
		logger.setLevel(CanaryLevel.PLUGIN_DEBUG);
		for (java.util.logging.Handler h:logger.getParent().getHandlers()) {
			h.setLevel(CanaryLevel.ALL);
		}
	}
	
	public static Logman get() {
		if (logger != null) {
			return logger;
		} else {
			throw new NullPointerException("Logger.get called before initialize");
		}
	}
	
}
