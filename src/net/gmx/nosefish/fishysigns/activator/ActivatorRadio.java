package net.gmx.nosefish.fishysigns.activator;

public class ActivatorRadio<T> implements Activator {
	private final String bandName;
	private final T signal;
	
	public ActivatorRadio(String bandName, T signal) {
		this.signal = signal;
		this.bandName = bandName;
	}
	
	public String getBandName() {
		return bandName;
	}
	
	public T getSignal() {
		return signal;
	}
	


}
