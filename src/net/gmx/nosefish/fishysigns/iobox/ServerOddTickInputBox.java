package net.gmx.nosefish.fishysigns.iobox;

import net.gmx.nosefish.fishysigns.anchor.IAnchor;
import net.gmx.nosefish.fishysigns.exception.UnsupportedActivatorException;
import net.gmx.nosefish.fishysigns.watcher.ServerOddTickWatcher;
import net.gmx.nosefish.fishysigns.watcher.activator.ActivatorServerTick;
import net.gmx.nosefish.fishysigns.watcher.activator.IActivator;



public class ServerOddTickInputBox extends AnchoredActivatableBox {

	public static interface IServerOddTickHandler extends IAnchor {
		/**
		 * 
		 * @param tickNumber
		 *     the number of ticks as reported by the ServerTicker, different from
		 *     Canary's tick count.
		 */ 
		public void handleServerOddTick(long tickNumber);
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
	public void initActivatable() {
		ServerOddTickWatcher.getInstance().register(this.getID());
	}

	@Override
	public void activate(IActivator activator) {
		if((activator == null)
           || (! ActivatorServerTick.class.equals(activator.getClass()))) {
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
