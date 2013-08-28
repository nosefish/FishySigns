package net.gmx.nosefish.fishysigns.signs;

import java.util.Arrays;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import net.gmx.nosefish.fishylib.blocks.BlockInfo;
import net.gmx.nosefish.fishylib.worldmath.FishyDirection;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishylib.worldmath.FishyVectorInt;
import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.gmx.nosefish.fishysigns.Log;
import net.gmx.nosefish.fishysigns.activator.Activator;
import net.gmx.nosefish.fishysigns.activator.ActivatorRedstone;
import net.gmx.nosefish.fishysigns.activator.ImmutableRedstoneChange;
import net.gmx.nosefish.fishysigns.plugin.engine.UnloadedSign;
import net.gmx.nosefish.fishysigns.watcher.RedstoneChangeWatcher;
import net.gmx.nosefish.fishysigns.world.ChunkTracker;
import net.gmx.nosefish.fishysigns.world.WorldValuePublisher;

public abstract class RedstoneTriggeredFishySign extends FishySign {
	private static final FishyVectorInt[] relativeInputs = new FishyVectorInt[]{
	                                                       new FishyVectorInt(1,0,0),
	                                                       new FishyVectorInt(-1,0,0),
	                                                       new FishyVectorInt(0,0,1),
	                                                       new FishyVectorInt(0,0,-1)
	                                                       };
	// All instance variables are guarded by the "this" instance lock.
	protected FishyLocationInt[] inputBlocks;
	protected FishySignInput currentInput;
	// input location -> input pin number
	protected Map<FishyLocationInt, Integer> inputMap = new HashMap<FishyLocationInt, Integer>(5, 0.9f);
	// number of distinct inputs, set by mapInputs()
	protected int numberOfInputs = 0;
	

	/**
	 * Do not call this constructor directly.
	 * Use <code>FishySign.loadAndRegister</code> or <code>FishySign.createAndRegister</code> to instantiate new FishySigns.
	 * @param UnloadedSign sign
	 */
	protected RedstoneTriggeredFishySign(UnloadedSign sign) {
		super(sign);
		setInputBlocks(sign.getBlockType());
	}
	
	/**
	 * Used in constructor. Do not call from anywhere else.
	 * 
	 * @param signType
	 */
	protected synchronized void setInputBlocks(short signType) {
		FishyDirection front = this.direction;
		FishyDirection back = this.direction.opposite();
		FishyDirection left = FishyDirection.nearestDirection(this.direction.toDegrees() + 90);
		FishyDirection right = left.opposite();
		FishyLocationInt frontBlock = this.location.addIntVector(front.toUnitIntVector());
		FishyLocationInt leftBlock = this.location.addIntVector(left.toUnitIntVector());
		FishyLocationInt rightBlock = this.location.addIntVector(right.toUnitIntVector());
		FishyLocationInt backBlock = this.location.addIntVector(back.toUnitIntVector());
		if (signType == BlockType.WallSign.getId()) {
			this.inputBlocks = new FishyLocationInt[]{frontBlock, leftBlock, rightBlock};
			this.mapInputs(0, 1, 2);
		} else {
			// sign post
			if (FishyDirection.cardinalDirections.contains(this.direction)) {
				// oriented N/E/S/W
				this.inputBlocks = new FishyLocationInt[]{frontBlock, leftBlock, rightBlock, backBlock};
				this.mapInputs(0, 1, 2, 3);
			} else {
				// other orientation
				this.inputBlocks = new FishyLocationInt[relativeInputs.length];
				for (int i = 0; i < relativeInputs.length; ++i) {
					inputBlocks[i] = this.location.addIntVector(relativeInputs[i]); 
				}
				// no well-defined input directions -> recognize only one input
				this.mapInputs(0, 0, 0, 0);
			}
		}
	}

	
	// default order when looking at the text side of the sign,
	// front = 0, left = 1, right = 2, back = 3
	protected synchronized void mapInputs(Integer... order) {
		inputMap.clear();
		for (int i = 0; i < inputBlocks.length; ++i) {
			if (i >= order.length) {
				break;
			}
			inputMap.put(inputBlocks[i], order[i]);
		}
		this.numberOfInputs = (new TreeSet<Integer>(Arrays.asList(order))).size(); 
	}
	
	public synchronized int getNumberOfInputs() {
		return this.numberOfInputs;
	}
	
	public synchronized Integer getPin(FishyLocationInt input) {
		return this.inputMap.get(input);
	}
	
	/**
	 * {@inheritDoc}
	 * 
	 * When overriding this to allow other activation types,
	 * call <code>super(activator)</code> at least
	 * for instances of <code>ActivatorRedstone</code>.
	 */
	@Override
	public void activate(Activator activator) {
		if (! (activator instanceof ActivatorRedstone)) {
			return;
		}
		ActivatorRedstone rsActivator = (ActivatorRedstone) activator;
		Boolean[] updatedInputState;
		FishySignInput newInput;
		FishySignInput oldInput;
		// compute new input from currentInput, inputConnections, and changes in activator
		synchronized (this) {
			boolean[] inputConnections = getConnectionState();
			updatedInputState = new Boolean[this.getNumberOfInputs()];
			for (int i = 0; i < inputConnections.length; ++i) {
				// is this a direct input?
				if (inputConnections[i]) {
					int pin = this.getPin(inputBlocks[i]);
					// has it changed?
					for (ImmutableRedstoneChange change : rsActivator.getChanges()) {
						if (inputBlocks[i].equalsLocation(change.getLocation())) {
							//yes, it has changed
							if (updatedInputState[pin] == null) {
								updatedInputState[pin] = change.getNewLevel() > 0; 
							} else {
								// any input high -> pin is high
								updatedInputState[pin] = updatedInputState[pin] || change.getNewLevel() > 0;
							}
						}
					}
					if (updatedInputState[pin] == null) {
						// not changed, set to previous value
						updatedInputState[pin] = currentInput.getState(pin);
					}
				}
				//else: not a direct input, keep whatever value the pin has from previous iterations 
			}
			// At this point updatedInputState contains the new state of all pins.
			newInput = new FishySignInput(updatedInputState);
			oldInput = currentInput;
			currentInput = newInput;
		}
		if (! oldInput.equals(newInput)) {
			this.onRedstoneInputChange(oldInput, newInput);
		}
	}
	
	protected abstract void onRedstoneInputChange(FishySignInput oldInput, FishySignInput newInput);

	@Override
	public synchronized void initialize() {
		this.initializeCurrentInput();
		this.registerInputsWithWatcher();
	}
	
	@Override
	public void remove() {
		RedstoneChangeWatcher.getInstance().remove(this);
	}
	
	protected synchronized void initializeCurrentInput(){
		WorldValuePublisher.publish();
		Boolean[] input = new Boolean[this.getNumberOfInputs()];
		for (FishyLocationInt blockLoc : inputBlocks) {
			Block block = unsafeGetBlockAt(blockLoc);
			if (block == null) {
				continue;
			}
			short id = block.getTypeId();
			short data = block.getData();
			if (BlockInfo.isDirectInput(blockLoc, id, data, this.getLocation())) {
				boolean powered = BlockInfo.getRedstonePower(id, data) > 0;
				int pin = getPin(blockLoc);
				if (input[pin] == null) {
					input[pin] = powered; 
				} else {
					input[pin] = input[pin] || powered;
				}
			}
		}
		this.currentInput = new FishySignInput(input);
	}
	
	/**
	 * Will very rarely return wrong values when redstone related blocks are placed/broken
	 * at the inputs while the check is running. I doubt anyone will ever notice in game.
	 * 
	 * @return
	 */
	protected synchronized boolean[] getConnectionState() {
		boolean[] connectionState = new boolean[this.inputBlocks.length];
		for (int i = 0; i < connectionState.length; ++i) {
			FishyLocationInt blockLoc = this.inputBlocks[i];
			Block block = unsafeGetBlockAt(blockLoc);
			if (block == null) {
				continue;
			}
			short id = block.getTypeId();
			short data = block.getData();
			connectionState[i] = BlockInfo.isDirectInput(blockLoc, id, data, this.getLocation());
		}
		return connectionState;
	}
	
	/**
	 * This is not really thread-safe and will in some cases return inconsistent values.
	 * Only use when you don't mind that. Do not change the returned block!
	 * 
	 * @param blockLoc
	 * @return
	 */
	protected static Block unsafeGetBlockAt(FishyLocationInt blockLoc) {
		Block block = null;
		try {
			WorldValuePublisher.publish();
			if (ChunkTracker.getInstance().isChunkLoaded(blockLoc)) {
				block = blockLoc.getWorld().getWorldIfLoaded().getBlockAt(
						                                         blockLoc.getIntX(),
						                                         blockLoc.getIntY(),
						                                         blockLoc.getIntZ());
			}
		} catch (ConcurrentModificationException e) {
			// TODO: retry?
		} catch (Exception e) {
			// TODO: do something maybe?
		}
		return block;
	}
	
	protected synchronized void registerInputsWithWatcher() {
		for (FishyLocationInt blockLoc : inputBlocks) {
			Log.get().logInfo(this.getClass().getSimpleName() + " registering block " + blockLoc);
			RedstoneChangeWatcher.getInstance().register(this, blockLoc);
		}
	}
}
