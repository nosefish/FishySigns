package net.gmx.nosefish.fishysigns.signs;

import net.gmx.nosefish.fishylib.worldmath.FishyDirection;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.activator.Activator;
import net.gmx.nosefish.fishysigns.activator.ActivatorRedstone;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.signs.plumbing.FishyDirectInputBox;
import net.gmx.nosefish.fishysigns.signs.plumbing.FishySignSignal;
import net.gmx.nosefish.fishysigns.watcher.RedstoneChangeWatcher;


public abstract class RedstoneTriggeredFishySign extends FishySign {
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
	 * Default wires all blocks next to the sign to a single input pin.
	 */
	protected void initializeInputBox() {
		FishyLocationInt[] inputLocations = this.getInputLocations();
		inputBox = new FishyDirectInputBox(this.location, inputLocations.length, 1);
		inputBox.setAllInputPins(inputLocations);
		inputBox.wireAllToPin0();
		inputBox.finishInit();
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
