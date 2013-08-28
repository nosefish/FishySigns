package net.gmx.nosefish.fishysigns.signs;

import java.util.Arrays;

/**
 * Input state for a RedstoneTriggeredFishySign. Immutable.
 * 
 * @author Stefan Steinheimer
 *
 */
public class FishySignInput {
	Boolean[] inputs;
	
	public FishySignInput(Boolean... inputs) {
		this.inputs = inputs.clone();
	}
	
	public int getNumberOfPins() {
		return inputs.length;
	}
	
	public Boolean getState(int pin) {
		return inputs[pin];
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
		FishySignInput other = (FishySignInput) obj;
		return Arrays.equals(inputs, other.inputs);
	}
	
	@Override
	public String toString() {
		StringBuilder build = new StringBuilder(8);
		build.append("[");
		for (int i = 0; i < inputs.length; ++i) {
			Boolean b = inputs[i];
			if (b == null) {
				build.append("null");
			} else {
				build.append(b.booleanValue());
			}
			if (i < inputs.length - 1) {
				build.append(", ");
			}
		}
		build.append("]");
		return build.toString();
	}
	
}
