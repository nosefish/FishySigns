package net.gmx.nosefish.fishysigns.signs;



import net.gmx.nosefish.fishysigns.activator.Activator;
import net.gmx.nosefish.fishysigns.activator.ActivatorPlayer;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.watcher.PlayerRightClickWatcher;

public abstract class FishyMechanismSign extends RedstoneTriggeredFishySign {

	public FishyMechanismSign(UnloadedSign sign) {
		super(sign);
	}

	@Override
	public void activate(Activator activator) {
		if (activator instanceof ActivatorPlayer) {
			ActivatorPlayer ap = (ActivatorPlayer)activator;
			this.onPlayerRightClick(ap.getPlayerName());
		} else {
			super.activate(activator);
		}
	}

	@Override
	public void initialize() {
		PlayerRightClickWatcher.getInstance().register(this, this.getLocation());
	}
	
	protected abstract void onPlayerRightClick(String playerName);	
}
