package net.gmx.nosefish.fishysigns.iobox;

import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.anchor.IAnchor;
import net.gmx.nosefish.fishysigns.exception.UnsupportedActivatorException;
import net.gmx.nosefish.fishysigns.watcher.PlayerRightClickWatcher;
import net.gmx.nosefish.fishysigns.watcher.activator.ActivatorPlayerRightClick;
import net.gmx.nosefish.fishysigns.watcher.activator.IActivator;
import net.gmx.nosefish.fishysigns.world.FishyLocationBlockState;


public class RightClickInputBox extends AnchoredActivatableBox {
	public static interface IRightClickInputHandler extends IAnchor {
		public void handleRightClick(String playerName, FishyLocationBlockState block);
	}
	
	private final FishyLocationInt location;
	private final IRightClickInputHandler handler;
	
	public static RightClickInputBox createAndRegister(FishyLocationInt location,
	                                            IRightClickInputHandler handler) {
		RightClickInputBox box = new RightClickInputBox(location, handler);
		registerWithActivationManagerAndAnchor(box, handler);
		return box;
	}
	
	private RightClickInputBox(FishyLocationInt location, IRightClickInputHandler handler) {
		this.location = location;
		this.handler = handler;
	}

	@Override
	public void initialize() {
		PlayerRightClickWatcher.getInstance().register(this.getID(), location);
	}

	@Override
	public void activate(IActivator activator) {
		if (! ActivatorPlayerRightClick.class.equals(activator.getClass())) {
			String aClass = ((activator == null) ? "null" : activator.getClass().getSimpleName());
			throw new UnsupportedActivatorException("Expected "
					+ ActivatorPlayerRightClick.class.getSimpleName()
					+ ", but received "
					+ aClass);
		}
		ActivatorPlayerRightClick aprc = (ActivatorPlayerRightClick)activator;
		handler.handleRightClick(aprc.getPlayerName(), aprc.getBlockState());
	}

	@Override
	public void remove() {
		PlayerRightClickWatcher.getInstance().remove(this.getID());
	}

}
