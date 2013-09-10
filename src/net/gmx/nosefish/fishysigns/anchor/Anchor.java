package net.gmx.nosefish.fishysigns.anchor;

import java.util.ArrayList;
import java.util.List;

/**
 * An object that holds references
 * to IAnchorables and notifies them
 * when the reference is removed.
 * 
 * Thread-safe.
 * 
 * @author Stefan Steinheimer
 *
 */
public class Anchor implements IAnchor {

	private Object lock = new Object();
	private List<IAnchorable> anchoredObjects = new ArrayList<IAnchorable>(4);
	private boolean raised = false;
	
	public final void anchor(IAnchorable toAnchor) {
		synchronized(lock) {
			if (raised) {
				toAnchor.anchorRaised(this);
			} else {
				anchoredObjects.add(toAnchor);
			}
		}
	}
	
	public final void raiseAnchor(IAnchorable toRaise) {
		synchronized(lock) {
			if (anchoredObjects.remove(toRaise)) {
				toRaise.anchorRaised(this);
			}
		}
	}
	
	/**
	 * Tells all IAnchorable that this Anchor no
	 * longer references them.
	 * 
	 * To be called by subclasses.
	 */
	public final synchronized void raiseAnchor() {
		synchronized (lock) {
			raised = true;
			for (IAnchorable anchoredObject : anchoredObjects) {
				anchoredObject.anchorRaised(this);
			}
			this.anchoredObjects.clear();
		}
	}
}
