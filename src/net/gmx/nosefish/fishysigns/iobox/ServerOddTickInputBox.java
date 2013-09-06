package net.gmx.nosefish.fishysigns.iobox;

import net.gmx.nosefish.fishysigns.anchor.IAnchor;
import net.gmx.nosefish.fishysigns.exception.UnsupportedActivatorException;
import net.gmx.nosefish.fishysigns.watcher.ServerOddTickWatcher;
import net.gmx.nosefish.fishysigns.watcher.activator.ActivatorServerTick;
import net.gmx.nosefish.fishysigns.watcher.activator.IActivator;



public class ServerOddTickInputBox extends AnchoredActivatableBox {

	public static interface IServerOddTickHandler extends IAnchor {
		public void handleServerOddTick(int tickNumber);
	}
	
	private final IServerOddTickHandler handler;
	

	public static ServerOddTickInputBox createAndRegister(IServerOddTickHandler handler) {
		ServerOddTickInputBox box = new ServerOddTickInputBox(handler);
		registerWithActivationManagerAndAnchor(box, handler);
		return box;
	}
	
	private ServerOddTickInputBox(IServerOddTickHandler handler) {
		this.handler = handler;
	}
	
	@Override
	public void initialize() {
		ServerOddTickWatcher.getInstance().register(this.getID());
	}

	@Override
	public void activate(IActivator activator) {
		if(! ActivatorServerTick.class.equals(activator.getClass())) {
			String aClass = ((activator == null) ? "null" : activator.getClass().getSimpleName());
			throw new UnsupportedActivatorException("Expected "
					+ ActivatorServerTick.class.getSimpleName()
					+ ", but received "
					+ aClass);
		}
		ActivatorServerTick ast = (ActivatorServerTick)activator;
		handler.handleServerOddTick(ast.getTick());
	}

	@Override
	public void remove() {
		ServerOddTickWatcher.getInstance().remove(this.getID());
	}

}