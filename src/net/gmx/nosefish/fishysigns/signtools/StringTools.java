package net.gmx.nosefish.fishysigns.signtools;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringTools {
	
	/**
	 * Converts first occurrence of pattern
	 * to upper case. if the pattern is not found, 
	 * the input string is returned.
     * @param pattern
     * @param inputString
     * @return 
	 */
	public static String patternInStringToUpperCase(Pattern pattern, String inputString) {
		Matcher matcher = pattern.matcher(inputString);
		if (matcher.find()) {
			String replaced = matcher.replaceFirst(matcher.group().toUpperCase());
			return replaced;
		}
		return inputString;
	}
	
	public static String replaceAmpersandWithParagraph(String inputString) {
		return inputString.replace('&', '\u00A7');
	}
}
