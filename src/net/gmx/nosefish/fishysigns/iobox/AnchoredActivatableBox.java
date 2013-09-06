package net.gmx.nosefish.fishysigns.iobox;

import net.gmx.nosefish.fishysigns.anchor.Anchor;
import net.gmx.nosefish.fishysigns.anchor.IAnchor;
import net.gmx.nosefish.fishysigns.anchor.IAnchorable;
import net.gmx.nosefish.fishysigns.plugin.engine.ActivationManager;
import net.gmx.nosefish.fishysigns.watcher.activator.IActivatable;

public abstract class AnchoredActivatableBox implements IAnchorable, IActivatable{
	private volatile Long id = IActivatable.ID_UNINITIALIZED;
	
	@Override
	public long getID() {
		return id;
	}


	@Override
	public void setID(long id) {
		if (this.id == IActivatable.ID_UNINITIALIZED) {
			this.id = id;
		}
	}
	
	@Override
	public void anchorRaised(Anchor anchor) {
		ActivationManager.getInstance().remove(this.getID());
	}
	
	/**
	 * Do not call in constructor!
	 */
	protected static void registerWithActivationManagerAndAnchor(AnchoredActivatableBox box, IAnchor anchor) {
		ActivationManager.getInstance().register(box);
		anchor.anchor(box);
	}
	

}
