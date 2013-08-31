package net.gmx.nosefish.fishysigns.signs.plumbing;

import java.util.ArrayList;

import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.gmx.nosefish.fishylib.blocks.BlockInfo;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.task.common.OutputLever;
import net.gmx.nosefish.fishysigns.world.Unsafe;

public class FishyLeverOutputBox {
	
	protected Object lock = new Object();
	protected final ArrayList<FishyLocationInt> physOutput;
	protected final boolean[] physSignal;
	
	
	public FishyLeverOutputBox(int pinCount) {
		this.physOutput = new ArrayList<FishyLocationInt>(pinCount);
		this.physSignal = new boolean[pinCount];
	}
	
	public void setOutputLocation(int pin, FishyLocationInt location){
		synchronized(lock) {
			physOutput.add(pin, location);
		}
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
				Block block = Unsafe.unsafeGetBlockAt(location);
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
	
	public FishySignSignal getSignal() {
		synchronized(lock) {
			return new FishySignSignal(physSignal);
		}
	}
	
	public void updateOutput(FishySignSignal signal) {
		synchronized(lock) {
			int lowerPinCount = Math.min(getPinCount(), signal.getNumberOfPins());
			for (int pin = 0; pin < lowerPinCount; pin++) {
				physSignal[pin] = signal.getState(pin);
			}
			refreshOutput();
		}
	}
	
	public void refreshOutput() {
		synchronized(lock) {
			for (int pin = 0; pin < getPinCount(); ++pin) {
				OutputLever.set(physOutput.get(pin), physSignal[pin]);
			}
		}
	}
}
