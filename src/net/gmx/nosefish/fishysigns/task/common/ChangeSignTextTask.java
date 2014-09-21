package net.gmx.nosefish.fishysigns.task.common;

import net.canarymod.api.world.World;
import net.canarymod.api.world.blocks.Sign;
import net.canarymod.api.world.blocks.TileEntity;
import net.gmx.nosefish.fishylib.worldmath.FishyChunk;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.task.FishyTask;

/**
 * Changes the text on a sign to the one passed in the constructor.
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 */
public class ChangeSignTextTask extends FishyTask {
	private static final int NUM_LINES = 4;
	private static final int LINE_LENGTH = 15;
	
	private final FishyLocationInt signLoc;
	private final String[] newText;
	
	/**
	 * Constructor
	 * 
	 * @param signLocation
	 *     the location of the sign to change.
	 *     
	 * @param newText
	 *     the text to write on the sign. Must have length 4! <code>null</code> lines will be ignored
	 */
	public ChangeSignTextTask(FishyLocationInt signLocation, String[] newText) {
		this.signLoc = signLocation;
		this.newText = newText;
		if (newText.length != NUM_LINES) {
			throw new InvalidSignTextException("Invalid number of lines, expected " + 
			                                    NUM_LINES +
			                                    ", but received " + 
			                                    newText.length);
		}
	}

	@Override
	public void doStuff() {
		World world = signLoc.getWorld().getWorldIfLoaded();
		if (world == null) {
			return;
		}
		if (! world.isChunkLoaded(
				FishyChunk.worldToChunk(signLoc.getIntX()), 
				FishyChunk.worldToChunk(signLoc.getIntZ()))) {
			return;
		}
		TileEntity te = world.getOnlyTileEntityAt(signLoc.getIntX(), signLoc.getIntY(), signLoc.getIntZ());
		if ((te != null) && (te instanceof Sign)) {
			Sign sign = (Sign) te;
			for (int line = 0; line < NUM_LINES; ++line){
				if (newText[line] != null) {
					if (newText[line].length() > LINE_LENGTH) {
						throw new InvalidSignTextException("Line too long: '" + 
                                newText[line] +
                                "' has " + 
                                newText.length +
                                " characters, but only " +
                                LINE_LENGTH +
                                " are allowed on a sign.");
					}
					sign.setTextOnLine(newText[line], line);
				}
			}
			sign.update();
		}
	}
	
	public class InvalidSignTextException extends RuntimeException {
		private static final long serialVersionUID = -1342781346250422909L;

		public InvalidSignTextException() {
			super();
		}
		
		public InvalidSignTextException(String message) {
			super(message);
		}
	}

}
