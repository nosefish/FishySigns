package net.gmx.nosefish.fishysigns.signs;

import net.gmx.nosefish.fishylib.worldmath.FishyDirection;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.iobox.DirectInputBox;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.iobox.DirectInputBox.IDirectInputHandler;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;


public abstract class RedstoneTriggeredFishySign
              extends FishySign
           implements IDirectInputHandler {
	
	protected volatile DirectInputBox inputBox;

	@Override
	public void initialize() {
		this.initializeDirectInputBox();
	}

	/**
	 * Called in <code>initialize</code>. Override to your liking.
	 * Default wires all blocks next to the sign to a single input pin.
	 */
	protected void initializeDirectInputBox() {
		FishyLocationInt[] inputLocations = this.getInputLocations();
		inputBox = DirectInputBox.createAndRegister(this.location, inputLocations.length, 1, this);
		inputBox.setAllInputPins(inputLocations);
		inputBox.wireAllToPin0();
		inputBox.finishInit();
	}
	
	protected DirectInputBox getDirectInputBox() {
		return inputBox;
	}
	
	/**
	 * Called by initializeInputBox.
	 * @return the input locations
	 */
	protected FishyLocationInt[] getInputLocations() {
		FishyLocationInt[] result; 
		if (this.isWallSign()) {
			result = new FishyLocationInt[] {
			                 this.getLocation().addIntVector(FRONT.toUnitIntVector()),
			                 this.getLocation().addIntVector(LEFT.toUnitIntVector()),
			                 this.getLocation().addIntVector(RIGHT.toUnitIntVector())
			             };
		} else {
			result = new FishyLocationInt[] {
			                 this.getLocation().addIntVector(FishyDirection.NORTH.toUnitIntVector()),
			                 this.getLocation().addIntVector(FishyDirection.EAST.toUnitIntVector()),
			                 this.getLocation().addIntVector(FishyDirection.SOUTH.toUnitIntVector()),
			                 this.getLocation().addIntVector(FishyDirection.WEST.toUnitIntVector())
			             };
		}
		return result;
	}

	protected boolean isRisingEdge(IOSignal oldS, IOSignal newS, int pin) {
		return (! oldS.getState(pin) && newS.getState(pin));
	}
	
	protected boolean isFallingEdge(IOSignal oldS, IOSignal newS, int pin) {
		return (oldS.getState(pin) && ! newS.getState(pin));
	}
	
	
	protected boolean isChange(IOSignal oldS, IOSignal newS) {
		return oldS.equals(newS);
	}
	/**
	 * Do not call this constructor directly.
	 * Use <code>FishySign.loadAndRegister</code> or <code>FishySign.createAndRegister</code> to instantiate new FishySigns.
	 * @param UnloadedSign sign
	 */
	public RedstoneTriggeredFishySign(UnloadedSign sign) {
		super(sign);
	}
}
