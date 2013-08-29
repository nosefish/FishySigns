package net.gmx.nosefish.fishysigns.signs;

import net.gmx.nosefish.fishysigns.activator.Activator;
import net.gmx.nosefish.fishysigns.activator.ActivatorPlayer;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.watcher.PlayerRightClickWatcher;

public abstract class FishyRightClickSign extends FishySign {

	public FishyRightClickSign(UnloadedSign sign) {
		super(sign);
	}

	protected abstract void onPlayerRightClick(String PlayerName);
	
	@Override
	public void initialize() {
		PlayerRightClickWatcher.getInstance().register(this, this.getLocation());
	}

	@Override
	public void activate(Activator activator) {
		if (! (activator instanceof ActivatorPlayer)) {
			return;
		}
		ActivatorPlayer ap = (ActivatorPlayer) activator;
		onPlayerRightClick(ap.getPlayerName());
	}

	@Override
	public void remove() {
		PlayerRightClickWatcher.getInstance().remove(this);
	}
}
