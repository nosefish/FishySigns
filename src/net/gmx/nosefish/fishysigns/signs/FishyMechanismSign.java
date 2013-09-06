package net.gmx.nosefish.fishysigns.signs;


import net.gmx.nosefish.fishysigns.iobox.RightClickInputBox;
import net.gmx.nosefish.fishysigns.iobox.RightClickInputBox.IRightClickInputHandler;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;

public abstract class FishyMechanismSign 
              extends RedstoneTriggeredFishySign
           implements IRightClickInputHandler{

	public FishyMechanismSign(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public void initialize() {
		RightClickInputBox.createAndRegister(this.getLocation(), this);
	}
}
