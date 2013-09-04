package net.gmx.nosefish.fishysigns.activator;

//TODO: the generic type argument gets removed by the ActivationManager,
// resulting in an uncheckd cast for the Activatable. That is just wrong.
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
