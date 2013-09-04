package net.gmx.nosefish.fishysigns.radio;

import java.util.Set;
import java.util.TreeSet;

import net.gmx.nosefish.fishysigns.activator.ActivatorRadio;
import net.gmx.nosefish.fishysigns.plugin.engine.ActivationManager;

/**
 * A radio band. All registered receivers will
 * receive every broadcast made.
 * 
 * The signal should usually be an immutable type,
 * although there are cases where a thread-safe
 * mutable type is appropriate.
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 * @param <T> the type of signal to be transmitted on this band
 */
public class RadioBand<T> {
	private final String name;
	// synchronize on receivers!
	private final Set<Long> receivers = new TreeSet<Long>();
	private volatile T signal = null;
	
	/**
	 * Constructor
	 * 
	 * @param name
	 *     the name of the radio band
	 */
	public RadioBand(String name) {
		this.name = name;
	}
	
	/**
	 * Gets the most recent broadcast made on this band
	 * 
	 * @return
	 *     the last signal transmitted, or <code>null</code> if there was none
	 */
	public T getLastBroadcast() {
		return signal;
	}
	
	/**
	 * Broadcasts a new signal. Activates all registered
	 * <code>Activatables</code>
	 * 
	 * @param signal
	 *     the signal to send
	 */
	public void newBroadcast(T signal) {
		this.signal = signal;
		Long[] toActivate;
		synchronized(receivers) {
			if (receivers.isEmpty()) {
				return;
			}
			toActivate = receivers.toArray(new Long[receivers.size()]);
		}
		ActivatorRadio<T> activator = new ActivatorRadio<T>(name, signal);
		ActivationManager.getInstance().activateAll(activator, toActivate);
	}

	/**
	 * Registers an <code>Activatable</code> to
	 * receive transmissions from this band.
	 * 
	 * @param receiverID
	 *     the <code>Activatable.getID()</code>
	 */
	public void register(Long receiverID) {
		synchronized(receivers) {
			receivers.add(receiverID);
		}
	}
	
	/**
	 * Removes an <code>Activatable</code>
	 * from the list of receivers.
	 * 
	 * @param receiverID
	 *     the <code>Activatable.getID()</code>
	 */
	public void remove(Long receiverID) {
		synchronized(receivers) {
			receivers.remove(receiverID);
		}
	}
	
	/**
	 * Finds out if anyone is listening
	 * 
	 * @return
	 *     <code>true</code> if <code>Activatables</code> are registered,
	 *     <code>false</code> if the list of receivers is empty
	 */
	public boolean hasListeners() {
		synchronized (receivers) {
			return (! receivers.isEmpty());
		}	
	}
	
	/**
	 * Gets the band name of this RadioBand
	 * 
	 * @return
	 *     the band name
	 */
	public String getName() {
		return name;
	}
}
