package net.gmx.nosefish.fishysigns.signs;

import net.gmx.nosefish.fishylib.worldmath.FishyDirection;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishylib.worldmath.FishyVectorInt;
import net.gmx.nosefish.fishysigns.activator.Activator;
import net.gmx.nosefish.fishysigns.activator.ActivatorRedstone;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.signs.plumbing.FishyDirectInputBox;
import net.gmx.nosefish.fishysigns.signs.plumbing.FishySignSignal;
import net.gmx.nosefish.fishysigns.watcher.RedstoneChangeWatcher;


public abstract class RedstoneTriggeredFishySign extends FishySign {
	private static final FishyVectorInt[] relativeInputs = new FishyVectorInt[]{
	                                                       new FishyVectorInt(1,0,0),
	                                                       new FishyVectorInt(-1,0,0),
	                                                       new FishyVectorInt(0,0,1),
	                                                       new FishyVectorInt(0,0,-1)
	                                                       };
	
	protected FishyDirectInputBox inputBox;

	protected abstract void onRedstoneInputChange(FishySignSignal oldInput, FishySignSignal newInput);
	
	@Override
	public void initialize() {
		this.initializeInputBox();
		this.registerInputsWithWatcher();
	}

	/**
	 * {@inheritDoc}
	 * 
	 * When overriding this to allow other activation types,
	 * call <code>super(activator)</code> at least
	 * for instances of <code>ActivatorRedstone</code>.
	 */
	@Override
	public void activate(Activator activator) {
		if (! (activator instanceof ActivatorRedstone)) {
			return;
		}
		ActivatorRedstone rsActivator = (ActivatorRedstone) activator;
		FishySignSignal oldInput = inputBox.getSignal();
		inputBox.updateInput(rsActivator.getChanges());
		FishySignSignal newInput = inputBox.getSignal();
		if (! oldInput.equals(newInput)) {
			this.onRedstoneInputChange(oldInput, newInput);
		}
	}

	@Override
	public void remove() {
		RedstoneChangeWatcher.getInstance().remove(this);
	}

	/**
	 * Called in <code>initialize</code>. Override to your liking.
	 */
	protected void initializeInputBox() {
		if (FishyDirection.cardinalDirections.contains(this.direction)) {
			FishyLocationInt frontBlock = location.addIntVector(FRONT.toUnitIntVector());
			FishyLocationInt leftBlock = location.addIntVector(LEFT.toUnitIntVector());
			FishyLocationInt rightBlock = location.addIntVector(RIGHT.toUnitIntVector());
			FishyLocationInt backBlock = location.addIntVector(BACK.toUnitIntVector());
			if (this.isWallSign()) {
				int pinCount = 3;
				inputBox = new FishyDirectInputBox(location, pinCount, pinCount);
				inputBox.setAllInputPins(new FishyLocationInt[]{frontBlock, leftBlock, rightBlock});
				inputBox.wireOneToOne();
			} else {
				// sign post
				int pinCount = 4;
				if (FishyDirection.cardinalDirections.contains(direction)) {
					// oriented N/E/S/W
					inputBox = new FishyDirectInputBox(location, pinCount, pinCount);
					inputBox.setAllInputPins(new FishyLocationInt[]{frontBlock, leftBlock, rightBlock, backBlock});
					inputBox.wireOneToOne();

				}
			}
		} else {
			// sign post in other orientation
			// no well-defined input directions -> only one sign input
			int pinCount = 4;
			inputBox = new FishyDirectInputBox(this.location, pinCount, 1);
			for (int pin = 0; pin < pinCount; ++pin) {
				FishyLocationInt inputLocation = this.location.addIntVector(relativeInputs[pin]);
				inputBox.setInputPinLocation(pin, inputLocation); 
			}
			inputBox.wireAllToPin0();
		}
		inputBox.finishInit();
	}

	
	protected void registerInputsWithWatcher() {
		for (FishyLocationInt blockLoc : inputBox.getInputLocations()) {
			RedstoneChangeWatcher.getInstance().register(this, blockLoc);
		}
	}
	
	/**
	 * Do not call this constructor directly.
	 * Use <code>FishySign.loadAndRegister</code> or <code>FishySign.createAndRegister</code> to instantiate new FishySigns.
	 * @param UnloadedSign sign
	 */
	protected RedstoneTriggeredFishySign(UnloadedSign sign) {
		super(sign);
	}
}
