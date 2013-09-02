package net.gmx.nosefish.fishysigns.signs;

import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishylib.worldmath.FishyVectorInt;
import net.gmx.nosefish.fishysigns.activator.Activator;
import net.gmx.nosefish.fishysigns.activator.ActivatorPlayerRightClick;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.signs.plumbing.FishyLeverOutputBox;
import net.gmx.nosefish.fishysigns.signs.plumbing.FishySignSignal;
import net.gmx.nosefish.fishysigns.task.common.MessagePlayerTask;
import net.gmx.nosefish.fishysigns.watcher.PlayerRightClickWatcher;

/**
 * A RedstoneTriggeredFishySign with output support. Must be a WallSign.
 * Default setup is all three input blocks wired to a single input pin
 * and one output lever. (Like CraftBook's <i>SISO</i>)
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 */
public abstract class FishyICSign extends RedstoneTriggeredFishySign {
	protected FishyLeverOutputBox outputBox;
	

	@Override
	public void initialize() {
		super.initialize();
		initializeInputBox();
		initializeOutputBox();
		FishyLocationInt[] outputLocations = outputBox.getOutputLocations();
		for (FishyLocationInt loc : outputLocations) {
			PlayerRightClickWatcher.getInstance().register(this.getID(), loc);
		}
	}
	
	protected void updateOutput(FishySignSignal newSignal) {
		this.outputBox.updateOutput(newSignal);
	}
	
	/**
	 * Sets up the <code>outputBox</code>.
	 * If you need more than 1 pin, override
	 */
	protected void initializeOutputBox() {
		outputBox = new FishyLeverOutputBox(1);
		outputBox.setOutputLocation(0, this.getCentreOutput(1));
		outputBox.finishInit();
	}
	
	/**
	 * Gets location for an output behind the sign,
	 * attached to the block that is <code>distance</code> blocks behind the sign.
	 * 
	 * @param distance
	 * @return the location of that block
	 */
	protected FishyLocationInt getCentreOutput(int distance) {
		FishyVectorInt offset = this.BACK.toUnitIntVector()
		                                 .scalarIntMult(distance + 1);
		return this.location.addIntVector(offset);
	}
	
	/**
	 * Gets location for an output on the left side of the sign,
	 * attached to the block that is <code>distance</code> blocks behind the sign.
	 * 
	 * @param distance
	 * @return the location of that block
	 */
	protected FishyLocationInt getLeftOutput(int distance) {
		FishyVectorInt offset = this.BACK.toUnitIntVector()
		                                 .scalarIntMult(distance)
		                                 .addInt(this.LEFT.toUnitIntVector());
		return this.location.addIntVector(offset);
	}
	
	/**
	 * Gets location for an output on the right side of the sign,
	 * attached to the block that is <code>distance</code> blocks behind the sign.
	 * 
	 * @param distance
	 * @return the location of that block
	 */
	protected FishyLocationInt getRightOutput(int distance) {
		FishyVectorInt offset = this.BACK.toUnitIntVector()
		                                 .scalarIntMult(distance)
		                                 .addInt(this.RIGHT.toUnitIntVector());
		return this.location.addIntVector(offset);
	}
	
	@Override
	public void activate(Activator activator) {
		super.activate(activator);
		// If a player changes an output lever, reset it. That'll show him not to mess with a FishySign!
		// Also, we send him a message to tell him what's going on.
		if (activator instanceof ActivatorPlayerRightClick) {
			ActivatorPlayerRightClick ap = (ActivatorPlayerRightClick) activator;
			FishyLocationInt clickLocation = ap.getBlockState().getLocation();
			int pin = outputBox.getPin(clickLocation);
			if (pin != -1) {
				outputBox.refreshOutput();
				MessagePlayerTask sendMsg = new MessagePlayerTask(ap.getPlayerName(), "Please do not click IC outputs!");
				sendMsg.submit();
			}
		}
	}
	
	@Override
	public void remove() {
		super.remove();
		PlayerRightClickWatcher.getInstance().remove(this.getID());
	}
	
	@Override
	public boolean validateOnLoad() {
		return this.isWallSign();
	}

	@Override
	public boolean validateOnCreate(String playerName) {
		return this.isWallSign();
	}
	
	public FishyICSign(UnloadedSign sign) {
		super(sign);
	}

}
