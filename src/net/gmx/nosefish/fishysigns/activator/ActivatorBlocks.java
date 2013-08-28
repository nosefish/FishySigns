package net.gmx.nosefish.fishysigns.activator;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;


public class ActivatorBlocks implements Activator {
	protected final List<ImmutableBlockStateChange> blocks = new LinkedList<ImmutableBlockStateChange>();
	
	public ActivatorBlocks() {
	}
	
	public void add(ImmutableBlockStateChange change) {
		blocks.add(change);
	}
	
	public List<ImmutableBlockStateChange> getBlockStateChanges() {
		return Collections.unmodifiableList(blocks);
	}

}
