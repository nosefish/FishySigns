package net.gmx.nosefish.fishysigns.signs;

import net.gmx.nosefish.fishylib.worldmath.FishyDirection;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.blocks.BlockType;
import net.canarymod.api.world.blocks.Sign;
import net.gmx.nosefish.fishysigns.Log;
import net.gmx.nosefish.fishysigns.anchor.Anchor;
import net.gmx.nosefish.fishysigns.anchor.BlockAnchor;
import net.gmx.nosefish.fishysigns.anchor.IAnchorable;
import net.gmx.nosefish.fishysigns.plugin.engine.FishySignClassLoader;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.task.FishyTask;
import net.gmx.nosefish.fishysigns.task.common.ChangeSignTextTask;

/**
 * All FishySigns must be thread safe! Only methods that are documented to be called
 * from the server thread may access Minecraft resources directly!
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 */
public abstract class FishySign extends Anchor implements IAnchorable {
	public static final String[] EMPTY_SIGN_TEXT = { "", "", "", "" };
	
	// useful relative directions
	protected final FishyDirection FRONT;
	protected final FishyDirection LEFT;
	protected final FishyDirection RIGHT;
	protected final FishyDirection BACK;
	
	protected String[] text; // synchronize on "this"
	protected final FishyLocationInt location;
	protected final FishyDirection direction;
	protected final short type;

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
				Log.get().info(player.getName() 
                                  + " has created a " 
                                  + fishySign.getClass().getSimpleName()
                                  + " at " + fishySign.getLocation());
				anchorSign(fishySign);
				startInitTask(fishySign);
			}
		}
		return (fishySign != null);
	}

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
				anchorSign(fishySign);
				startInitTask(fishySign);
			}
		}
		return (fishySign != null);
	}

	private static void startInitTask(FishySign fs) {
		InitTask init = new InitTask(fs);
		init.submit();
	}
	
	/**
	 * Creates a BlockAnchor and anchors the sign to that.
	 * 
	 * @param fishySign
	 *     the sign to anchor
	 */    
	private static void anchorSign(FishySign fishySign) {
		// The anchor will be referenced from the ActivationManager until the block is
		// broken or unloaded.
		Anchor blockAnchor = BlockAnchor.createAndRegister(fishySign.getLocation());
		blockAnchor.anchor(fishySign);
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
     * @param playerName
	 * @return <code>true</code>, if the sign is valid and should be registered as a FishySign, <code>false</code> if it is invalid.
	 */
	public abstract boolean validateOnCreate(String playerName);

	/**
	 * Called when the sign has been successfully loaded or created.
	 * set up you Input-/OutputBoxes here.
	 * 
	 * Also, call super.initialize() unless you want to completely redefine it.
	 * It's ugly, but still better than all alternatives I can think of.
	 */
	protected abstract void initialize();
	
	/**
	 * Called when the sign is unloaded.
	 * Either chunk containing it has been unloaded,
	 * or the sign has been destroyed.
	 * 
	 * Default implementation in FishySigns classes is empty.
	 * No need to call super.unload(). It's rarely needed;
	 * defining it as abstract would introduce clutter in
	 * subclasses.
	 */
	protected void onUnload() {
		// nothing in here
	}
	
	public final FishyLocationInt getLocation() {
		return this.location;
	}

	public final boolean isWallSign() {
		return this.type == BlockType.WallSign.getId();
	}
	
	public final boolean isSignPost() {
		return this.type == BlockType.SignPost.getId();
	}

	/**
	 * Gets text on the specified line of the sign (0-3).
	 * 
	 * @param line
	 *     the line number
	 *     
	 * @return
	 *     the text on the line
	 */
	public final synchronized String getLine(int line) {
		return this.text[line];
	}
	
	public final synchronized void setLine(int line, String newText) {
		this.text[line] = newText;
	}
	
	/**
	 * Gets a copy of the sign text
	 * 
	 * @return
	 *     the sign text
	 */
	public final synchronized String[] getTextCopy() {
		return text.clone();
	}
	
	
	/**
	 * Sets the text of this FishySign but does not update it
	 * in the world.
	 * 
	 * @param newText
	 *     the text to set
	 */
	protected final synchronized void setText(String[] newText){
		for (int line = 0; line < 4; ++line) {
			this.text = newText.clone();
		}
	}
	
	/**
	 * Writes the sign text to the sign in the world.
	 * 
	 * Rather expensive operation, only call when you're
	 * done with all your changes.
	 */
	protected synchronized void updateSignTextInWorld() {
		ChangeSignTextTask signUpdater = new ChangeSignTextTask(this.getLocation(), this.getTextCopy());
		signUpdater.submit();
	}


	@Override
	public void anchorRaised(Anchor anchor) {
        this.raiseAnchor();
		this.onUnload();
	}
	
	
	/**
	 * Do not call this constructor directly.
	 * Use <code>loadAndRegister</code> or <code>createAndRegister</code> to instantiate new FishySigns.
     * @param sign
	 */
	public FishySign(UnloadedSign sign) {
		this.location = sign.getLocation();
		this.text = sign.getText();
		this.type = sign.getBlockType();
		this.direction = sign.getFacingDirection();
		this.FRONT = direction;
		this.BACK  = direction.opposite();
		this.LEFT  = FishyDirection.nearestDirection(direction.toDegrees() + 90);
		this.RIGHT = LEFT.opposite();
	}
	
	protected static class InitTask extends FishyTask {
		private final FishySign fs;
		
		public InitTask(FishySign fs){
			this.fs = fs;
		}
		
		@Override
		public void doStuff() {
			fs.initialize();
		}
		
	}
}
