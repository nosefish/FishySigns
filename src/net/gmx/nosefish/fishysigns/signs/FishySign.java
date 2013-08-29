package net.gmx.nosefish.fishysigns.signs;

import java.util.concurrent.atomic.AtomicLong;


import net.gmx.nosefish.fishylib.worldmath.FishyDirection;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.Sign;
import net.gmx.nosefish.fishysigns.activator.Activatable;
import net.gmx.nosefish.fishysigns.plugin.engine.ActivationManager;
import net.gmx.nosefish.fishysigns.plugin.engine.FishySignClassLoader;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;

/**
 * All FishySigns must be thread safe! Only methods that are documented to be called
 * from the server thread may access Minecraft resources directly!
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 */
public abstract class FishySign implements Activatable {
	public static final String[] EMPTY_SIGN_TEXT = { "", "", "", "" };
	protected String[] text;
	protected final FishyLocationInt location;
	protected final FishyDirection direction;
	protected final short type;
	private final AtomicLong id; // will only be changed once during sign creation
	
	
	/**
	 * Do not call this constructor directly.
	 * Use <code>loadAndRegister</code> or <code>createAndRegister</code> to instantiate new FishySigns.
	 * @param UnloadedSign sign
	 */
	protected FishySign(UnloadedSign sign) {
		this.id = new AtomicLong(-1);
		this.location = sign.getLocation();
		this.text = sign.getText();
		this.type = sign.getBlockType();
		this.direction = sign.getFacingDirection();
	}

	@Override
	public FishyLocationInt getLocation() {
		return this.location;
	}

	@Override
	public final long getID() {
		return id.get();
	}
	
	@Override
	public final void setID(long id) {
		if (this.id.get() == -1) {
			this.id.set(id);
		}
	}

	public final boolean isWallSign() {
		return this.type == BlockType.WallSign.getId();
	}
	
	public final boolean isSignPost() {
		return this.type == BlockType.SignPost.getId();
	}

	public String getLine(int line) {
		return this.text[line];
	}
	/**
	 * Called when a sign is loaded that matches the <code>@FishySignIdentifier</code> regex.
	 * This might be outside the server thread. Do not access the world.
	 * Do not wait for results of tasks you start. Do not count on this being called when a chunk is loaded -
	 * the sign could be cached! Do not put any initialization code here!
	 * 
	 * @return <code>true</code>, if the sign is valid and should be registered as a FishySign, <code>false</code> if it is invalid.
	 */
	public abstract boolean validateOnLoad();
	
	/**
	 * Called when a sign is created that matches the <code>@FishySignIdentifier</code> regex.
	 * This will always run in the server thread. It is safe to access the world.
	 * Do not perform any long-running operations here. Remember that
	 * the sign is not registered yet - do not put initialization code here!
	 * 
	 * @param player name of the player who created this sign.
	 * @return <code>true</code>, if the sign is valid and should be registered as a FishySign, <code>false</code> if it is invalid.
	 */
	public abstract boolean validateOnCreate(String playerName);
	
	/**
	 * Factory method to instantiate a FishySign.
	 * Called by FishySignLoaderTask when a sign is found in a newly loaded chunk.
	 * 
	 * @param sign
	 * @return the newly loaded and registered FishySign, or null if it wasn't a valid FishySign
	 */
	public static boolean loadAndRegister(UnloadedSign sign) {
		FishySign fishySign = FishySignClassLoader.getInstance().makeFishySign(sign);
		if (fishySign != null) {
			if (fishySign.validateOnLoad()) {
				ActivationManager.getInstance().register(fishySign);
			}
		}
		return (fishySign != null);
	}

	/**
	 * Factory method to instantiate a FishySign.
	 * Called by FishyEngineListener when a sign is placed by a player.
	 * Must be called from the server thread.
	 * 
	 * @param sign
	 * @param player
	 * @return
	 */
	public static boolean createAndRegister(Sign sign, Player player) {
		FishySign fishySign = FishySignClassLoader.getInstance().makeFishySign(new UnloadedSign(sign));
		if (fishySign != null) {
			if (fishySign.validateOnCreate(player.getName())) {
				ActivationManager.getInstance().register(fishySign);
			}
		}
		return (fishySign != null);
	}
}
