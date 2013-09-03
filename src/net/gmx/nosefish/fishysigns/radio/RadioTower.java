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

	private ReentrantLock receiverLock = new ReentrantLock();
	private ConcurrentMap<String, RadioBand<T>> bands = new ConcurrentHashMap<String, RadioBand<T>>();
	
	public void broadcast(String bandName, T signal) {
		bands.putIfAbsent(bandName, new RadioBand<T>(bandName));
		bands.get(bandName).newBroadcast(signal);
	}
	
	public T getLastBroadcast(String bandName) {
		RadioBand<T> band = bands.get(bandName);
		return (band != null) ?
				band.getLastBroadcast(): null;
	}
	
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
	
	public void stopListening(Long id, String bandName) {
		RadioBand<T> band = bands.get(bandName);
		if (band != null) {
			band.remove(id);
			this.removeIfNobodyListens(band);
		}
	}
	
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
