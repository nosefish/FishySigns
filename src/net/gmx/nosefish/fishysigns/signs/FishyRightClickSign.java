package net.gmx.nosefish.fishysigns.signs;

import net.gmx.nosefish.fishysigns.iobox.RightClickInputBox;
import net.gmx.nosefish.fishysigns.iobox.RightClickInputBox.IRightClickInputHandler;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;


public abstract class FishyRightClickSign extends FishySign implements IRightClickInputHandler {

	public FishyRightClickSign(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public void initialize() {
		// will automatically be anchored to this sign
		RightClickInputBox.createAndRegister(this.getLocation(), this);
	}
	

}
