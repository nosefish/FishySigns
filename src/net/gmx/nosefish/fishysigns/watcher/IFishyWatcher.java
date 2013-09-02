package net.gmx.nosefish.fishysigns.watcher;

import net.canarymod.plugin.PluginListener;

/**
 * All IFishyWatchers must be singletons and
 * retain an instance reference somewhere; the plugin
 * only holds a weak reference.
 * 
 * When the reference which they have registered with the plugin
 * becomes null, they will automatically be removed and have to be
 * re-added.
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 */
public interface IFishyWatcher extends PluginListener {
	/**
	 * Called by the plugin to notify the watcher that
	 * it may start to process events.
	 */
	public void enable();
	
	/**
	 * Called by the plugin to notify the watcher that
	 * it has been disabled and that the watcher should
	 * therefore stop processing events.
	 */
	public void disable();
}
