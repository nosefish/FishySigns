package net.gmx.nosefish.fishysigns.watcher.activator;

import java.util.LinkedList;
import java.util.List;

public class ActivatorRedstone implements IActivator {
	private List<FishyRedstoneChange> changes = new LinkedList<FishyRedstoneChange>();

	public List<FishyRedstoneChange> getChanges() {
		return changes;
	}
	
	public void addChange(FishyRedstoneChange change) {
		changes.add(change);
	}
}
