package net.gmx.nosefish.fishysigns.radio;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;



/**
 * Wireless communication for FishySigns.
 * 
 * @author 
 *     Stefan Steinheimer (nosefish)
 *
 * @param <T> 
 *     the type of signals that this tower can broadcast.
 */
public class RadioTower<T> {
	// used for tuneIn and remove
	private final ReentrantLock receiverLock = new ReentrantLock();
	
	private final ConcurrentMap<String, RadioBand<T>> bands = new ConcurrentHashMap<>();
	
	/**
	 * Broadcast a signal.
	 * 
	 * @param bandName
	 *     the band to transmit on
	 *     
	 * @param signal
	 *     the signal to transmit
	 */
	public void broadcast(String bandName, T signal) {
		bands.putIfAbsent(bandName, new RadioBand<T>(bandName));
		bands.get(bandName).newBroadcast(signal);
	}
	
	
	/**
	 * Gets the last signal that was sent on the specified band.
	 * 
	 * @param bandName
	 *     the band to check
	 *     
	 * @return
	 *     The most recently transmitted signal. 
	 *     <code>null</code> if the band does not exist or if nothing has been transmitted yet.
	 */
	public T getLastBroadcast(String bandName) {
		RadioBand<T> band = bands.get(bandName);
		return (band != null) ?
				band.getLastBroadcast(): null;
	}
	
	/**
	 * Register an <code>Activatable</code> to receive signals
	 * that are broadcast over the specified band 
	 * (as <code>ActivatorRadio</code>). 
	 * 
	 * You must register even those <code>Activatables</code>
	 * that only use getLastBroadcast, because the band will 
	 * cease to exist when the last receiver is removed.
	 * 
	 * @param id
	 *     the id of the Activatable (use <code>getID</code>)
	 *     
	 * @param bandName
	 *     the bandName
	 */
	public void tuneIn(Long id, String bandName) {
		// we have to lock so that we can't remove a
		// band while adding a receiver
		receiverLock.lock();
		try {
			bands.putIfAbsent(bandName, new RadioBand<T>(bandName));
			bands.get(bandName).register(id);
		} finally {
			receiverLock.unlock();
		}
	}
	
	/**
	 * Remove the activatable from the list of receivers.
	 * Call this in your <code>remove</code> method for every
	 * band for which you have previously called <code>tuneIn</code>. 
	 * 
	 * @param id
	 *     the id of the Activatable (use <code>getID</code>)
	 *     
	 * @param bandName
	 *     the band name from which to unregister
	 */
	public void stopListening(Long id, String bandName) {
		RadioBand<T> band = bands.get(bandName);
		if (band != null) {
			band.remove(id);
			this.removeIfNobodyListens(band);
		}
	}
	
	/**
	 * Remove a band entirely if it has no receivers.
	 * Don't want memory leaks, however tiny.
	 * 
	 * @param band
	 *     the band to check
	 */
	private void removeIfNobodyListens(RadioBand<T> band) {
		// we have to lock so that we can't remove a
		// band while adding a receiver
		receiverLock.lock();
		try {
			if (! band.hasListeners()) {
				bands.remove(band.getName());
			}
		} finally {
			receiverLock.unlock();
		}
	}
}
