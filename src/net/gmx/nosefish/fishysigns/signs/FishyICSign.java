package net.gmx.nosefish.fishysigns.signs;

import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishylib.worldmath.FishyVectorInt;
import net.gmx.nosefish.fishysigns.iobox.FishySignSignal;
import net.gmx.nosefish.fishysigns.iobox.LeverOutputBox;
import net.gmx.nosefish.fishysigns.iobox.RightClickInputBox;
import net.gmx.nosefish.fishysigns.iobox.RightClickInputBox.IRightClickInputHandler;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.task.common.MessagePlayerTask;
import net.gmx.nosefish.fishysigns.world.FishyLocationBlockState;

/**
 * A RedstoneTriggeredFishySign with output support. Must be a WallSign.
 * Default setup is all three input blocks wired to a single input pin
 * and one output lever. (Like CraftBook's <i>SISO</i>)
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 */
public abstract class FishyICSign
              extends RedstoneTriggeredFishySign
           implements IRightClickInputHandler{
	
	protected LeverOutputBox outputBox;
	

	@Override
	public void initialize() {
		initializeRSInputBox();
		initializeOutputBox();
		initializeOutputLeverClickProtection();
	}
	
	/**
	 * Sets up the <code>outputBox</code>.
	 * If you need more than 1 pin, override
	 */
	protected void initializeOutputBox() {
		outputBox = new LeverOutputBox(1);
		outputBox.setOutputLocation(0, this.getCentreOutput(1));
		outputBox.finishInit();
	}
	
	/**
	 * Sets up RighClickInputBoxes for the output levers.
	 */
	// TODO: this should be in LeverOutputBox - would make it a LeverIOBox :)
	protected void initializeOutputLeverClickProtection() {
		FishyLocationInt[] outputLocations = outputBox.getOutputLocations();
		for (FishyLocationInt loc : outputLocations) {
			RightClickInputBox.createAndRegister(loc, this);
		}
	}
	
	/**
	 * Updates the output with the new signal.
	 * 
	 * @param newSignal
	 */
	protected void updateOutput(FishySignSignal newSignal) {
		this.outputBox.updateOutput(newSignal);
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
	public void handleRightClick(String playerName, FishyLocationBlockState block) {
		FishyLocationInt clickLocation = block.getLocation();
		int pin = outputBox.getPin(clickLocation);
		if (pin != -1) {
			outputBox.refreshOutput();
			MessagePlayerTask sendMsg = new MessagePlayerTask(playerName, "Why is this still in FishyICSign?");
			sendMsg.submit();
		}
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
