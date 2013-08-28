package net.gmx.nosefish.fishysigns.activator;

import java.util.LinkedList;
import java.util.List;

public class ActivatorRedstone implements Activator {
	private List<ImmutableRedstoneChange> changes = new LinkedList<ImmutableRedstoneChange>();

	public List<ImmutableRedstoneChange> getChanges() {
		return changes;
	}
	
	public void addChange(ImmutableRedstoneChange change) {
		changes.add(change);
	}
}
