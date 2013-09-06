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
public abstract class Anchor implements IAnchor {
	private List<IAnchorable> anchoredObjects = new ArrayList<IAnchorable>(4);
	
	public final synchronized void anchor(IAnchorable toAnchor) {
		anchoredObjects.add(toAnchor);
	}
	
	/**
	 * Tells all IAnchorable that this Anchor no
	 * longer references them.
	 * 
	 * To be called by subclasses.
	 */
	protected final synchronized void raiseAnchor() {
		for (IAnchorable anchoredObject : anchoredObjects) {
			anchoredObject.anchorRaised(this);
		}
		this.anchoredObjects = null;
	}
}
