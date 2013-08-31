package net.gmx.nosefish.fishysigns.signs.plumbing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.canarymod.api.world.blocks.Block;
import net.gmx.nosefish.fishylib.blocks.BlockInfo;
import net.gmx.nosefish.fishylib.worldmath.FishyLocationInt;
import net.gmx.nosefish.fishysigns.activator.ImmutableRedstoneChange;
import net.gmx.nosefish.fishysigns.world.Unsafe;

/**
 * Input module for a FishySign. Thread-safe.
 * 
 * To initialize:
 * <ul>
 * <li>Construct with FishyLocation of the sign and the desired number of pins.</li>
 * <li> Set an input pin location for <i>every</i> physical input pin</li>
 * <li> Wire the physical pins to the sign inputs. A physical input can only have one outgoing
 * connection, but sign inputs can have any number of incoming connections, including 0.
 * (sign input that aren't connected to a physical input are always <code>false</code></li>
 * <li> Call <code>finishInit</code>.</li>
 * </ul>
 * 
 * The values passed to the constructor cannot be changed. The inputs and wiring
 * can be changed any time.<br>
 * A change to the wiring should be followed by a call to <code>refreshSignSignal</code>.<br>
 * A change to the locations should be followed by <code>updateInputPinFromWorld</code>
 * for a single location, or <code>updateInputFromWorld</code> for all,
 * again followed by<code>refreshSignSignal</code>.
 * 
 * @author Stefan Steinheimer (nosfish)
 *
 */
public class FishyDirectInputBox {
	
	protected FishyLocationInt boxLocation;
	
	// Lock for all mutable members. Could be divided up, but that'd be overkill.
	// I don't expect much contention.
	protected final Object lock = new Object();
	
	// physical side
	protected final ArrayList<FishyLocationInt> physInput;
	protected final boolean[] physSignal;

	// wiring
	protected final Map<Integer, Integer> phys2sign;
	
	// sign side
	protected final boolean[] signSignal;

	
	public FishyDirectInputBox(FishyLocationInt inputBoxLocation, int physicalPinCount, int signPinCount) {
		this.boxLocation = inputBoxLocation;
		this.physInput = new ArrayList<FishyLocationInt>(physicalPinCount);
		this.physSignal = new boolean[physicalPinCount];
		this.phys2sign = new TreeMap<Integer, Integer>();
		this.signSignal = new boolean[signPinCount];
	}
	
	
	public void setInputPinLocation(int physicalPin, FishyLocationInt location) {
		synchronized(lock) {
			physInput.add(physicalPin, location);
		}
	}
	
	
	public void setAllInputPins(FishyLocationInt[] locationArray) {
		if (locationArray.length != getPhysicalPinCount()) {
			throw new IllegalArgumentException("length of locationArray does not match pin count");
		}
		synchronized (lock) {
			for (int pin = 0; pin < locationArray.length; pin++) {
				physInput.add(pin, locationArray[pin]);
			}
		}
	}
	
	
	public void wirePhysicalToSignPin(int physicalPin, int signPin) {
		synchronized(lock) {
			if (physicalPin < 0 || getPhysicalPinCount() - 1 < physicalPin) {
				throw new IllegalArgumentException("physicalPin out of range");
			}
			if (signPin < 0 || getSignPinCount() - 1 < physicalPin) {
				throw new IllegalArgumentException("physicalPin out of range");
			}
			phys2sign.put(physicalPin, signPin);
		}
	}
	
	
	public void wireAllToPin0() {
		synchronized(lock) {
			for (int pin = 0; pin < getPhysicalPinCount(); pin++) {
				phys2sign.put(pin, 0);
			}
		}
	}
	
	
	public void wireOneToOne(){
		synchronized(lock) {
			int lowerPinCount = Math.min(getPhysicalPinCount(), getSignPinCount());
			for (int i = 0; i < lowerPinCount; i++) {
				phys2sign.put(i, i);
			}
		}
	}
	
	
	public void finishInit() {
		synchronized(lock) {
			updateInputFromWorld();
			refreshSignSignal();
		}
	}
	
	
	public FishySignSignal getSignal() {
		synchronized(lock) {
			return new FishySignSignal(signSignal);
		}
	}
	
	
	public int getPhysicalPinCount() {
		return physSignal.length;
	}

	
	public int getSignPinCount() {
		return signSignal.length;
	}

	
	public FishyLocationInt[] getInputLocations() {
		synchronized(lock) {
			return physInput.toArray(new FishyLocationInt[getPhysicalPinCount()]);
		}
	}
	
	
	public void updateInput(List<ImmutableRedstoneChange> rsChanges) {
		boolean hasChanged;
		synchronized(lock) {
			for (int pin = 0; pin < getPhysicalPinCount(); ++pin) {
				hasChanged = false;
				// Assumption: only one change per input block per Activator
				for (ImmutableRedstoneChange change : rsChanges) {
					FishyLocationInt changeLocation = change.getLocation();
					if (physInput.get(pin).equals(changeLocation)){
						hasChanged = true;
						short id = change.getBlockState().getTypeId();
						short data = change.getBlockState().getData();
						if (Unsafe.unsafeGetDirectInput(changeLocation, id, data, boxLocation)) {
							physSignal[pin] = change.getNewLevel() > 0;
							break;
						} else {
							physSignal[pin] = false;
							break;
						}
					}
				}
				// hooks are not very reliable right now
				// better check for sneaky changes from broken or placed blocks
				if (! hasChanged) {
					updateInputPinFromWorld(pin);
				}
			}
			refreshSignSignal();
		}
	}

	
	public void refreshSignSignal() {
		synchronized(lock) {
			for (int pin = 0; pin < getSignPinCount(); pin++) {
				signSignal[pin] = false;
			}
			for (int pin = 0; pin < getPhysicalPinCount(); pin++) {
				signSignal[phys2sign.get(pin)] |= physSignal[pin];
			}
		}
	}
	
	
	public void updateInputFromWorld() {
		synchronized(lock) {
			for (int pin = 0; pin < getPhysicalPinCount(); pin++) {
				updateInputPinFromWorld(pin);
			}
		}
	}
	
	
	public void updateInputPinFromWorld(int pin){
		synchronized(lock) {
			physSignal[pin] = false;
			FishyLocationInt loc = physInput.get(pin);
			if (loc == null) {
				return;
			}
			Block block = Unsafe.unsafeGetBlockAt(loc);
			if (block == null) {
				return;
			}
			Boolean isDirect = Unsafe.unsafeGetDirectInput(loc, block.getTypeId(), block.getData(), boxLocation);
			if (isDirect != null && isDirect) {
				physSignal[pin] = BlockInfo.getRedstonePower(block.getTypeId(), block.getData()) > 0;
			}
		}
	}
}
