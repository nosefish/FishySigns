package net.gmx.nosefish.fishysigns.signs;

import java.lang.ref.WeakReference;

import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishylib.worldmath.FishyVectorInt;
import net.gmx.nosefish.fishysigns.Log;
import net.gmx.nosefish.fishysigns.anchor.IAnchor;
import net.gmx.nosefish.fishysigns.anchor.IAnchorable;
import net.gmx.nosefish.fishysigns.iobox.IOSignal;
import net.gmx.nosefish.fishysigns.iobox.LeverIOBox;
import net.gmx.nosefish.fishysigns.iobox.LeverIOBox.ILeverIOHandler;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.task.common.MessagePlayerTask;


/**
 * A RedstoneTriggeredFishySign with output support. Must be a WallSign.
 * Default setup is all three input blocks wired to a single input pin
 * and one output lever. (Like CraftBook's <i>SISO</i>)
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 */
public abstract class FishyICSign
              extends RedstoneTriggeredFishySign {
	
	protected volatile LeverIOBox outputBox;
	

	@Override
	public void initialize() {
		initializeDirectInputBox();
		initializeOutputBox();
	}
	
	/**
	 * Sets up the <code>outputBox</code>.
	 * If you need more than 1 pin, override
	 */
	protected void initializeOutputBox() {
		outputBox = LeverIOBox.createAndRegister(1, new LeverClickBlocker(this));
		outputBox.setOutputLocation(0, this.getCentreOutput(1));
		outputBox.finishInit();
	}
	
	/**
	 * Gets the output box
	 * 
	 * @return
	 *     the output box
	 */
	protected LeverIOBox getOutputBox() {
		return outputBox;
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

	/**
	 * Anchors the LeverIOBox to the IAnchor passed in the constructor
	 * and blocks all clicks, sending a message to the offending player.
	 * 
	 * @author Stefan Steinheimer (nosefish)
	 *
	 */
	protected static class LeverClickBlocker implements ILeverIOHandler {
		private WeakReference<IAnchor> anchor;
		private static final String message = "Please do not click IC outputs!";
		
		public LeverClickBlocker(IAnchor anchor) {
			this.anchor = new WeakReference<IAnchor>(anchor);
		}
		
		@Override
		public void anchor(IAnchorable toAnchor) {
			anchor.get().anchor(toAnchor);
		}

		@Override
		public boolean handleIOLeverRightClick(
				String playerName, IOSignal currentSignal, int pinClicked) {
			
			MessagePlayerTask sendMsg = new MessagePlayerTask(playerName, message);
			sendMsg.submit();
			// always deny
			return false;
		}

		@Override
		public void handleIOLeverStateChanged(IOSignal oldSignal,
				IOSignal newSignal) {
			// we always deny, this shouldn't be called at all
			Log.get().warn("FishyICSign.LeverClickBlocker: " +
					"handleIOLeverStateChanged was called unexpectedly.");
		}
		
	}
}
