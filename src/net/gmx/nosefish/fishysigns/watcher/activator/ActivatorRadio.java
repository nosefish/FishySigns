package net.gmx.nosefish.fishysigns.watcher.activator;

//TODO: the generic type argument gets removed by the ActivationManager,
// resulting in an uncheckd cast for the Activatable. That is just wrong.
public class ActivatorRadio implements IActivator {
	private final String bandName;
	private final Object signal;
	
	public ActivatorRadio(String bandName, Object signal) {
		this.signal = signal;
		this.bandName = bandName;
	}
	
	public String getBandName() {
		return bandName;
	}
	
	public Object getSignal() {
		return signal;
	}
}
