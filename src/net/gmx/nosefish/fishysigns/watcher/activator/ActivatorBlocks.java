package net.gmx.nosefish.fishysigns.watcher.activator;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains a list of <code>ImmutableBlockStateChange</code> instances.
 * 
 * @author Stefan Steinheimer
 *
 */
public class ActivatorBlocks implements IActivator {
	protected final List<FishyBlockStateChange> blocks = new LinkedList<>();
	
	/**
	 * Constructor
	 */
	public ActivatorBlocks() {
	}
	
	/**
	 * Adds a change to the list. 
	 * To be used by the watcher issuing this.
	 * 
	 * @param 
	 *     change the block state change to add
	 */
	public void add(FishyBlockStateChange change) {
		blocks.add(change);
	}
	
	/**
	 * Gets the list of changes.
	 * To be used by the receiving <code>Activatable</code>
	 * 
	 * @return
	 */
	public List<FishyBlockStateChange> getBlockStateChanges() {
		return Collections.unmodifiableList(blocks);
	}

}
