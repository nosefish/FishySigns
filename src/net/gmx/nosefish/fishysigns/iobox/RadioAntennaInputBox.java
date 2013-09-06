package net.gmx.nosefish.fishysigns.iobox;

import net.gmx.nosefish.fishysigns.Log;
import net.gmx.nosefish.fishysigns.anchor.IAnchor;
import net.gmx.nosefish.fishysigns.exception.UnsupportedActivatorException;
import net.gmx.nosefish.fishysigns.radio.RadioTower;
import net.gmx.nosefish.fishysigns.watcher.activator.ActivatorRadio;
import net.gmx.nosefish.fishysigns.watcher.activator.IActivator;

public class RadioAntennaInputBox<T> extends AnchoredActivatableBox {
	public static interface IRadioInputHandler<T> extends IAnchor{
		public void handleRadioBroadcast(T message);
	}
	
	private final Class<T> signalType;
	private final RadioTower<T> radioTower;
	private final String bandName;
	private IRadioInputHandler<T> handler;
	
	public static <T> RadioAntennaInputBox<T> createAndRegister(
			RadioTower<T> tower,
			String bandName,
			IRadioInputHandler<T> handler,
			Class<T> signalType) {
		RadioAntennaInputBox<T> box = new RadioAntennaInputBox<T>(tower, bandName, handler, signalType);
		registerWithActivationManagerAndAnchor(box, handler);
		return box;
	}
	
	private RadioAntennaInputBox(
			RadioTower<T> tower,
			String bandName,
			IRadioInputHandler<T> handler,
			Class<T> signalType) {
		this.radioTower = tower;
		this.bandName = bandName;
		this.handler = handler;
		this.signalType = signalType;
	}
	
	public T getLastBroadcast() {
		return radioTower.getLastBroadcast(bandName); 
	}
	
	@Override
	public void initialize() {
		radioTower.tuneIn(this.getID(), bandName);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void activate(IActivator activator) {
		if (! ActivatorRadio.class.equals(activator.getClass())) {
			String aClass = ((activator == null) ? "null" : activator.getClass().getSimpleName());
			throw new UnsupportedActivatorException("Expected "
					+ ActivatorRadio.class.getSimpleName()
					+ ", but received "
					+ aClass);
		}
		ActivatorRadio ar = (ActivatorRadio) activator;
		Object object = ar.getSignal();
		T signal = null;
		if(object != null) {
			if (signalType.isAssignableFrom(object.getClass())){
				signal = (T)object;
			}
			else {
				Log.get().logWarning("Incompatible radio message! expected "
						+ signalType.getSimpleName()
						+ " but received "
						+ object.getClass().getSimpleName());
				return;
			}
		}
		handler.handleRadioBroadcast(signal);
	}

	@Override
	public void remove() {
		radioTower.stopListening(this.getID(), bandName);
	}

}
