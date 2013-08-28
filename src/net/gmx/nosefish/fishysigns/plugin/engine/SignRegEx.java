package net.gmx.nosefish.fishysigns.plugin.engine;

import java.util.regex.Pattern;

public final class SignRegEx {
	private final Pattern[] regEx = {null, null, null, null};
	private final String[] regExStrings = {null, null, null, null};
	private volatile int hashCache;
	private volatile boolean hashCached = false;;
	
	public SignRegEx(Pattern[] regExPatterns) {
		if (regExPatterns == null
				|| regExPatterns.length != 4) {
			throw new ArrayIndexOutOfBoundsException("Array must have length 4");
		}
		for (int i = 0; i < 4; i++) {
			if (regExPatterns[i] != null) { 
				this.regEx[i] = regExPatterns[i];
				this.regExStrings[i] = regExPatterns[i].toString();
			}
		}
	}
	
	public boolean matches(String[] signText) {
		for (int i = 0; i < 4; i++) {
			if (regEx[i] == null) {
				continue;
			}
			if (! regEx[i].matcher(signText[i]).matches()) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		if (hashCached) {
			return hashCache;
		}
		int tmpHash = 31;
		for (String s : regExStrings) {
			int stringHash = 0;
			if (s != null) {
				stringHash = s.hashCode();
			}
			tmpHash = 91 * tmpHash + stringHash;
		}
		hashCache = tmpHash;
		hashCached = true;
		return tmpHash;
	}
	
	@Override
	public boolean equals(Object other) {
		boolean isEqual = true;
		if (other instanceof SignRegEx) {
			SignRegEx otherSRE = (SignRegEx) other;
			for (int i = 0; i < 4; i++) {
				String my = this.regExStrings[i];
				String theirs = otherSRE.regExStrings[i];
				if (my != null && theirs != null) {
					// both not null -> check equals()
					isEqual = my.equals(theirs);
				} else {
					// both null -> equal
					isEqual = (my == theirs);
				}
				if (! isEqual) {
					// no need to check the rest
					break;
				}
			}
		} else {
			// different class
			isEqual = false;
		}
		return isEqual;
	}
}
