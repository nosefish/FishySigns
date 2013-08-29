package net.gmx.nosefish.fishysigns.signs.plumbing;

import java.util.Arrays;

/**
 * Input state for a RedstoneTriggeredFishySign. Immutable.
 * 
 * @author Stefan Steinheimer
 *
 */
public class FishySignSignal {
	boolean[] inputs;
	
	public FishySignSignal(boolean... inputs) {
		this.inputs = inputs.clone();
	}
	
	public int getNumberOfPins() {
		return inputs.length;
	}
	
	public boolean getState(int pin) {
		return inputs[pin];
	}
	
	public boolean[] toArray() {
		return inputs.clone();
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(inputs);
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		FishySignSignal other = (FishySignSignal) obj;
		return Arrays.equals(inputs, other.inputs);
	}
	
	@Override
	public String toString() {
		StringBuilder build = new StringBuilder(8);
		build.append("[");
		for (int i = 0; i < inputs.length; ++i) {
			build.append(inputs[i]);
			if (i < inputs.length - 1) {
				build.append(", ");
			}
		}
		build.append("]");
		return build.toString();
	}
	
}
