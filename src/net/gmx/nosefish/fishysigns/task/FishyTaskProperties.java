package net.gmx.nosefish.fishysigns.task;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum FishyTaskProperties {
	THREADSAFE, TIMEDELAYED, TICKDELAYED, TIMEREPEAT, TICKREPEAT;
	
	static Set<FishyTaskProperties> DELAYS = Collections.unmodifiableSet(EnumSet.of(TIMEDELAYED, TICKDELAYED));
	static Set<FishyTaskProperties> REPEATS = Collections.unmodifiableSet(EnumSet.of(TIMEREPEAT, TICKREPEAT));
}
