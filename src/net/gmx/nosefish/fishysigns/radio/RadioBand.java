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
 * mutable type is more appropriate.
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
	
	public RadioBand(String name) {
		this.name = name;
	}
	
	public T getLastBroadcast() {
		return signal;
	}
	
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

	public void register(Long receiverID) {
		synchronized(receivers) {
			receivers.add(receiverID);
		}
	}
	
	public void remove(Long receiverID) {
		synchronized(receivers) {
			receivers.remove(receiverID);
		}
	}
	
	public boolean hasListeners() {
		synchronized (receivers) {
			return (! receivers.isEmpty());
		}	
	}
	
	public String getName() {
		return name;
	}
}
