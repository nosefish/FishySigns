package net.gmx.nosefish.fishysigns.iobox;

import java.util.ArrayList;

import net.canarymod.api.world.blocks.BlockType;
import net.gmx.nosefish.fishylib.blocks.BlockInfo;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.anchor.IAnchor;
import net.gmx.nosefish.fishysigns.exception.DisabledException;
import net.gmx.nosefish.fishysigns.exception.UnsupportedActivatorException;
import net.gmx.nosefish.fishysigns.plugin.engine.ServerTicker;
import net.gmx.nosefish.fishysigns.task.common.SetLeverTask;
import net.gmx.nosefish.fishysigns.watcher.PlayerRightClickWatcher;
import net.gmx.nosefish.fishysigns.watcher.activator.ActivatorPlayerRightClick;
import net.gmx.nosefish.fishysigns.watcher.activator.IActivator;
import net.gmx.nosefish.fishysigns.world.FishyBlockState;
import net.gmx.nosefish.fishysigns.world.Unsafe;

public class LeverIOBox extends AnchoredActivatableBox {
	public static interface ILeverIOHandler extends IAnchor{
		/**
		 * Called when a player clicks an output lever and switches its state.
		 * 
		 * @param playerName
		 * @param oldSignal
		 * @param newSignal
		 * @return
		 *     true to allow the change, false to undo it.
		 */
		public boolean handleIOLeverRightClick (
				String playerName,
				IOSignal currentSignal,
				int pinClicked);
		
		/**
		 * Called after handleIOLeverClick has returned true to allow a change.
		 * 
		 * @param oldSignal
		 * @param newSignal
		 */
		public void handleIOLeverStateChanged (IOSignal oldSignal, IOSignal newSignal);
	}
	
	protected Object lock = new Object();
	protected final ArrayList<FishyLocationInt> physOutput;
	protected final boolean[] physSignal;
	
	protected final ILeverIOHandler handler;
	
	public static LeverIOBox createAndRegister(int pinCount, ILeverIOHandler handler) {
		LeverIOBox box = new LeverIOBox(pinCount, handler);
		registerWithActivationManagerAndAnchor(box, handler);
		return box;
	}
	
	public LeverIOBox(int pinCount, ILeverIOHandler handler) {
		this.physOutput = new ArrayList<FishyLocationInt>(pinCount);
		this.physSignal = new boolean[pinCount];
		this.handler = handler;
	}
	
	public void setOutputLocation(int pin, FishyLocationInt location){
		synchronized(lock) {
			physOutput.add(pin, location);
		}
		// update watcher registration
		this.remove();
		this.initialize();
	}
	
	public void setAllOutputLocations(FishyLocationInt[] locationArray) {
		if (locationArray.length != getPinCount()) {
			throw new IllegalArgumentException("length of locationArray does not match pin count");
		}
		synchronized (lock) {
			for (int pin = 0; pin < getPinCount(); pin++) {
				physOutput.add(pin, locationArray[pin]);
			}
		}
		// update watcher registration
		this.remove();
		this.initialize();
	}
	
	public void swapOutputs(int pin1, int pin2) {
		synchronized(lock) {
			FishyLocationInt loc1 = physOutput.get(pin1);
			boolean state1 = physSignal[pin1];
			physOutput.set(pin1, physOutput.get(pin2));
			physSignal[pin1] = physSignal[pin2];
			physOutput.set(pin2, loc1);
			physSignal[pin2] = state1;
		}
	}
	
	public void finishInit() {
		this.getOutputFromWorld();
	}
	
	public FishyLocationInt[] getOutputLocations() {
		synchronized(lock) {
			return physOutput.toArray(new FishyLocationInt[getPinCount()]);
		}
	}
	
	public int getPin(FishyLocationInt location) {
		synchronized(lock) {
			return physOutput.indexOf(location);
		}
	}

	public int getPinCount() {
		return physSignal.length;
	}
	
	public void getOutputFromWorld() {
		synchronized(lock) {
			for (int pin = 0; pin < getPinCount(); ++pin) {
				FishyLocationInt location = physOutput.get(pin);
				FishyBlockState block = Unsafe.unsafeGetBlockAt(location);
				physSignal[pin] = false;
				if (block == null) {
					continue;
				}
				short id = block.getTypeId();
				short data = block.getData();
				if (id == BlockType.Lever.getId()) {
					physSignal[pin] = BlockInfo.getRedstonePower(id, data) > 0;
				}
			}
		}
	}
	
	public IOSignal getSignal() {
		synchronized(lock) {
			return IOSignal.factory(physSignal);
		}
	}
	
	
	//-------------------------------------------------------------------------------------
	
	public void updateOutputNow(IOSignal signal) {
		long now = 0;
		try {
			now = ServerTicker.getInstance().getTickCount();
		} catch (DisabledException e) {
			// 0 will work, too
		}
		this.updateOutputOnTick(signal, now);
	}

	public void updateOutputOnTick(IOSignal signal, long targetTick) {
		synchronized(lock) {
			int lowerPinCount = Math.min(getPinCount(), signal.getNumberOfPins());
			for (int pin = 0; pin < lowerPinCount; pin++) {
				if (physSignal[pin] != signal.getState(pin)) {
					physSignal[pin] = signal.getState(pin);
					SetLeverTask setLever = new SetLeverTask(physOutput.get(pin), physSignal[pin], targetTick);
					setLever.submit();
				}
			}
			//refreshOutput();
		}
	}
	

	public void toggleOutputNow(int pin) {
		long now = 0;
		try {
			now = ServerTicker.getInstance().getTickCount();
		} catch (DisabledException e) {
			// 0 will work, too
		}
		toggleOutputOnTick(pin, now);
	}

	public void toggleOutputOnTick(int pin, long targetTick) {
		synchronized(lock) {
			this.physSignal[pin] = ! physSignal[pin];
			SetLeverTask setLever = new SetLeverTask(physOutput.get(pin), physSignal[pin], targetTick);
			setLever.submit();
		}
	}
	
	public void refreshOutput() {
		long now = 0;
		try {
			now = ServerTicker.getInstance().getTickCount();
		} catch (DisabledException e) {
			// 0 will work, too, but it doesn't matter, the plugin is shutting down
		}
		synchronized(lock) {
			for (int pin = 0; pin < getPinCount(); ++pin) {
				SetLeverTask setLever = new SetLeverTask(physOutput.get(pin), physSignal[pin], now);
				setLever.submit();
			}
		}
	}
	
	//------------------------------------------------------------------------
	// used by the engine
	//------------------------------------------------------------------------
	@Override
	public void activate(IActivator activator) {
		if (! ActivatorPlayerRightClick.class.equals(activator.getClass())) {
			String aClass = ((activator == null) ? "null" : activator.getClass().getSimpleName());
			throw new UnsupportedActivatorException("Expected "
					+ ActivatorPlayerRightClick.class.getSimpleName()
					+ ", but received "
					+ aClass);
		}
		ActivatorPlayerRightClick aprc = (ActivatorPlayerRightClick)activator;
		if (aprc.getBlockState().getTypeId() != BlockType.Lever.getId()) {
			// not a lever, so we don't care
			return;
		}
		IOSignal oldSignal = this.getSignal();
		// ask the handler if this is ok
		boolean allowed = handler.handleIOLeverRightClick(
				aprc.getPlayerName(), oldSignal, this.getPin(aprc.getBlockState().getLocation()));
		
		if (allowed) {
			// set new state and inform handler
			getOutputFromWorld();
			IOSignal newSignal = this.getSignal();
			handler.handleIOLeverStateChanged(oldSignal, newSignal);
		} else {
			// not allowed, reset to state before click
			refreshOutput();
		}
	}
	
	@Override
	public void remove() {
		PlayerRightClickWatcher.getInstance().remove(this.getID());
	}

	@Override
	protected void initActivatable() {
		for (FishyLocationInt location : physOutput) {
			PlayerRightClickWatcher.getInstance().register(this.getID(), location);
		}
	}
}
