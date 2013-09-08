package net.gmx.nosefish.fishysigns.signtools;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FishyParser {
	/**
	 * Non-matching parts of option strings will end up in a
	 * token with this key string.
	 */
	public static final String key_NO_MATCH = "_NO_MATCH_";
	
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
	 * Represents a syntax token. It is recommended to retrieve
	 * and validate the tokens in the <code>validateOnCreate</code>
	 * and <code>validateOnLoad</code> methods of FishySigns.
	 * 
	 * Caution: equality of tokens depends
	 * only on their name. Two Token instances with the
	 * same name but different values are considered equal.
	 * 
	 * @author Stefan Steinheimer (nosefish)
	 *
	 */
	public static class Token implements Comparable<Token>{
		private final String name;
		private volatile String value = null;

		public Token(String name) {
			this.name = name;
		}

		/**
		 * @return the value
		 */
		public String getValue() {
			return value;
		}

		/**
		 * @param value the value to set
		 */
		public void setValue(String value) {
			this.value = value;
		}

		/**
		 * @return the name
		 */
		public String getName() {
			return name;
		}


		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((name == null) ? 0 : name.hashCode());
			return result;
		}


		@Override
		public boolean equals(Object obj) {
			if (this == obj) {
				return true;
			}
			if (obj == null) {
				return false;
			}
			if (getClass() != obj.getClass()) {
				return false;
			}
			Token other = (Token) obj;
			if (name == null) {
				if (other.name != null) {
					return false;
				}
			} else if (!name.equals(other.name)) {
				return false;
			}
			return true;
		}

		@Override
		public int compareTo(Token other) {
			return this.name.compareTo(other.name);
		}


	}
	/**
	 * A rule consists of a Pattern that matches the
	 * substring that defines the parts of the sign
	 * and the token to set when that pattern is found.
	 * 
	 * @author Stefan Steinheimer (nosefish)
	 *
	 */
	public static class Rule {
		private final Pattern pattern;
		private final Token token;

		public Rule(Pattern pattern, Token option) {
			this.pattern = pattern;
			this.token = option;
		}
	}
	/**
	 * Checks the list of rules for matches with the tokenString.
	 * For each rule in the list (processed in order),
	 * it tries to find a matching substring in the optionString.
	 * When a match is found, it assigns the matching substring
	 * to the option with the matching pattern and removes 
	 * the substring from optionString.
	 * If the optionsStrig is not completely consumed at the end
	 * of this procedure, what is left of it will be placed in an
	 * option with the name Option.NO_MATCH and added to the
	 * returned Map.
	 * 
	 * If several Rules in the list contain Options with the same name,
	 * the last rule to match will define the value. All earlier matches
	 * will be discarded completely.
	 * 
	 * @param inputString
	 * @param rules
	 * @return
	 *     Map of option name to corresponding Option with decoded values
	 * 
	 */
	public static Map<String, Token> findTokens(String inputString, List<Rule> rules) {
		String restString = inputString;
		Map<String, Token> tokenMap = new LinkedHashMap<String, Token>(8);
		for (Rule rule : rules) {
			Matcher matcher = rule.pattern.matcher(restString); 
			if (matcher.find()) {
				restString = matcher.replaceFirst("");
				rule.token.setValue(matcher.group());
				tokenMap.put(rule.token.getName(), rule.token);
			}
		}
		if (! restString.isEmpty()) {
			Token noMatch = new Token(key_NO_MATCH);
			noMatch.setValue(restString);
			tokenMap.put(key_NO_MATCH, noMatch);
		}
		return tokenMap;
	}
}