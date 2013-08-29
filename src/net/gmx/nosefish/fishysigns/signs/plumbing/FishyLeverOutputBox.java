package net.gmx.nosefish.fishysigns.signs.plumbing;

import java.util.ArrayList;

import net.canarymod.api.world.blocks.Block;
import net.canarymod.api.world.blocks.BlockType;
import net.gmx.nosefish.fishylib.blocks.BlockInfo;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.task.common.OutputLever;
import net.gmx.nosefish.fishysigns.world.Unsafe;

public class FishyLeverOutputBox {
	
	protected final ArrayList<FishyLocationInt> physOutput;
	protected final boolean[] physSignal;
	
	
	public FishyLeverOutputBox(int pinCount) {
		this.physOutput = new ArrayList<FishyLocationInt>(pinCount);
		this.physSignal = new boolean[pinCount];
	}
	
	public void setOutputLocation(int pin, FishyLocationInt location){
		physOutput.add(pin, location);
	}
	
	public void swapOutputs(int pin1, int pin2) {
		FishyLocationInt loc1 = physOutput.get(pin1);
		boolean state1 = physSignal[pin1];
		physOutput.set(pin1, physOutput.get(pin2));
		physSignal[pin1] = physSignal[pin2];
		physOutput.set(pin2, loc1);
		physSignal[pin2] = state1;
	}
	
	public void finishInit() {
		this.getOutputFromWorld();
	}
	
	public FishyLocationInt[] getOutputLocations() {
		return null;
	}

	public int getPinCount() {
		return physSignal.length;
	}
	
	public void getOutputFromWorld() {
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
	
	public FishySignSignal getSignal() {
		return new FishySignSignal(physSignal);
	}
	
	public void updateOutput(FishySignSignal signal) {
		int lowerPinCount = Math.min(getPinCount(), signal.getNumberOfPins());
		for (int pin = 0; pin < lowerPinCount; pin++) {
			physSignal[pin] = signal.getState(pin);
		}
		refreshOutput();
	}
	
	public void refreshOutput() {
		for (int pin = 0; pin < getPinCount(); ++pin) {
			OutputLever.set(physOutput.get(pin), physSignal[pin]);
		}
	}
}
