package net.gmx.nosefish.fishysigns.activator;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Contains a list of <code>ImmutableBlockStateChange</code> instances.
 * 
 * @author Stefan Steinheimer
 *
 */
public class ActivatorBlocks implements Activator {
	protected final List<ImmutableBlockStateChange> blocks = new LinkedList<ImmutableBlockStateChange>();
	
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
	public void add(ImmutableBlockStateChange change) {
		blocks.add(change);
	}
	
	/**
	 * Gets the list of changes.
	 * To be used by the receiving <code>Activatable</code>
	 * 
	 * @return
	 */
	public List<ImmutableBlockStateChange> getBlockStateChanges() {
		return Collections.unmodifiableList(blocks);
	}

}
