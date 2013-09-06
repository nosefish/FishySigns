package net.gmx.nosefish.fishysigns.anchor;

import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.plugin.engine.ActivationManager;
import net.gmx.nosefish.fishysigns.watcher.BlockDestructionWatcher;
import net.gmx.nosefish.fishysigns.watcher.ChunkTracker;
import net.gmx.nosefish.fishysigns.watcher.activator.IActivatable;
import net.gmx.nosefish.fishysigns.watcher.activator.IActivator;

/**
 * Raises the Anchor if the block is broken or unloaded.
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 */
public class BlockAnchor extends Anchor implements IActivatable {
	private volatile Long id = IActivatable.ID_UNINITIALIZED;
	private final FishyLocationInt boxLocation;

	public static BlockAnchor createAndRegister(FishyLocationInt blockLocation) {
		BlockAnchor ba = new BlockAnchor(blockLocation);
		ActivationManager.getInstance().register(ba);
		return ba;
	}
	
	private BlockAnchor(FishyLocationInt blockLocation) {
		this.boxLocation = blockLocation;
	}
		
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
	public void initialize() {
		ChunkTracker.getInstance().register(id, boxLocation);
		BlockDestructionWatcher.getInstance().register(id, boxLocation);
	}

	@Override
	public void activate(IActivator activator) {
		// Whatever is in that activator, it can only mean one thing.
		ActivationManager.getInstance().remove(getID());
	}

	@Override
	public void remove() {
		ChunkTracker.getInstance().remove(getID());
		BlockDestructionWatcher.getInstance().remove(getID());
		this.raiseAnchor();
	}
}
