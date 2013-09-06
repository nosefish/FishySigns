package net.gmx.nosefish.fishysigns.anchor;

/**
 * Anchors that reference this will
 * call anchorRaised when the reference
 * is removed.
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 */
public interface IAnchorable {
	/**
	 * Called when an Anchor removes his reference.
	 * Method must be thread-safe!
	 * 
	 * @param anchor
	 *     the anchor that no longer holds a reference to this object
	 */
	public void anchorRaised(Anchor anchor);
}
