package net.gmx.nosefish.fishysigns.signtools;

import java.util.regex.Pattern;

public class PatternLib {
	/**
	 * Predefined Pattern: CraftBook-Extra input swap - any combination of "abc"
	 */
	public static final Pattern pattern_CBX_IN_SWAP = 
			Pattern.compile("abc|acb|bac|bca|cab|cba");
	
	/**
	 * Predefined Pattern: CraftBook-Extra output swap - any combination of "def"
	 */
	public static final Pattern pattern_CBX_OUT_SWAP = 
			Pattern.compile("def|dfe|edf|efd|fde|fed");
	
	/**
	 * Predefined Pattern: CraftBook self-triggered - just a capital "S"
	 */
	public static final Pattern pattern_CB_SELF_TRIGGERED =
			Pattern.compile("S");
	
	/**
	 * Predefined Pattern: the whole line, at least one character
	 */
	public static final Pattern pattern_NONEMPTY_STRING =
			Pattern.compile("^.+$");
	
	/**
	 * Predefined Pattern: blank line
	 */
	public static final Pattern pattern_EMPTY =
			Pattern.compile("^$");
	
	/**
	 * Predefined Pattern: integer number
	 */
	public static final Pattern pattern_POSITIVE_INTEGER =
			Pattern.compile("[0-9]+");

	
	/**
	 * Predefined Pattern: floating point number
	 */
	public static final Pattern pattern_POSITIVE_FLOAT =
			Pattern.compile("[0-9]+(\\.[0-9]+)?");
	
	/**
	 * Predefined Pattern: FishyVectorInt with components 
	 * delimited by ',' or ':'
	 */
	public static final Pattern pattern_FISHY_VECTOR_INT =
			Pattern.compile("([\\-]?[0-9]+[:,]){2}[\\-]?[0-9]+");
	
	/**
	 * Predefined Pattern: a colon
	 */
	public static final Pattern pattern_COLON =
			Pattern.compile(":");
}
