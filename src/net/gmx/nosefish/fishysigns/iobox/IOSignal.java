package net.gmx.nosefish.fishysigns.iobox;

import java.util.Arrays;

/**
 * Input/Output signal. Immutable.
 * 
 * @author Stefan Steinheimer (nosefish)
 *
 */
public class IOSignal {
	public static IOSignal ZERO_LENGTH = new IOSignal(new boolean[0]);
	
	public static IOSignal L = new IOSignal(false);
	public static IOSignal H = new IOSignal(true);
	
	public static IOSignal LL = new IOSignal(false, false);
	public static IOSignal HH = new IOSignal(true, true);
	public static IOSignal LH = new IOSignal(false, true);
	public static IOSignal HL = new IOSignal(true, false);
	
	public static IOSignal LLL = new IOSignal(false, false, false);
	public static IOSignal HHH = new IOSignal(true, true, true);
	public static IOSignal HLL = new IOSignal(true, false, false);
	public static IOSignal LHL = new IOSignal(false, true, false);
	public static IOSignal LLH = new IOSignal(false, false, true);
	public static IOSignal LHH = new IOSignal(false, true, true);
	public static IOSignal HLH = new IOSignal(true, false, true);
	public static IOSignal HHL = new IOSignal(true, true, false);

	private static final IOSignal[][] predef = new IOSignal[][] {
		{L, H},
		{LL, HH, LH, HL},
		{LLL, HHH, HLL, LHL, LLH, LHH, HLH, HHL}
	};
	
	// the actual signal
	private volatile boolean[] inputs;
	
	public static IOSignal factory(boolean... inputs) {
		if (inputs == null) {
			throw new NullPointerException("Expected array of boolean, got null.");
		}
		// is there a predefined signal for this?
		if (inputs.length == 0) {
			return ZERO_LENGTH;
		}
		if (inputs.length <= predef.length) {
			int pIndex = inputs.length - 1;
			for (IOSignal signal : predef[pIndex]) {
				if (Arrays.equals(signal.inputs, inputs)) {
					return signal;
				}
			}
		}
		// too long for a predefined signal, let's make a new one
		return new IOSignal(inputs);
	}
	
	private IOSignal(boolean... inputs) {
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

	public IOSignal getInverse() {
		if (this == H) {
			return L;
		} else if (this == L) {
			return H;
		}
		// longer signals will be inverted rarely
		// and a huge if-then-else construct probably
		// wouldn't be faster
		boolean[] sigArray = this.toArray();
		for (int i = 0; i < sigArray.length; i++) {
			sigArray[i] = ! sigArray[i];
		}
		return IOSignal.factory(sigArray);
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
		IOSignal other = (IOSignal) obj;
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
