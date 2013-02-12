package se.fnord.jamon;

import java.util.Arrays;
import java.util.HashSet;

public class CharacterMatchers {

	public static CharacterMatcher not(final CharacterMatcher matcher) {
		return new CharacterMatcher() {
			@Override
            public boolean match(char ch) {
				return !matcher.match(ch);
            }

			@Override
			public String toString() {
				return "not("+matcher+")";
			}
		};
	}

	public static CharacterMatcher not(char ... chars) {
		return not(match(chars));
	}

	public static CharacterMatcher and(final CharacterMatcher ... matchers) {
		return new CharacterMatcher() {
			@Override
            public boolean match(char ch) {
				for (CharacterMatcher matcher : matchers)
					if (!matcher.match(ch))
						return false;
				return true;
            }

			@Override
			public String toString() {
				return "and("+Arrays.toString(matchers)+")";
			}
		};
	}

	public static CharacterMatcher or(final CharacterMatcher ... matchers) {
		return new CharacterMatcher() {
			@Override
            public boolean match(char ch) {
				for (CharacterMatcher matcher : matchers)
					if (matcher.match(ch))
						return true;
				return false;
            }

			@Override
			public String toString() {
				return "or("+Arrays.toString(matchers)+")";
			}
		};
	}

	public static CharacterMatcher newline() {
		return match('\r', '\n');
	}

	public static CharacterMatcher letter() {
		return new CharacterMatcher() {
			@Override
			public boolean match(char ch) {
				return Character.isLetter(ch);
			}

			@Override
			public String toString() {
			    return "match[alpha]";
			}
		};
	}

	public static CharacterMatcher white() {
		return new CharacterMatcher() {
			@Override
			public boolean match(char ch) {
				return Character.isWhitespace(ch) && ch != '\n' && ch != '\r';
			}

			@Override
			public String toString() {
			    return "match[white]";
			}
		};
	}

	public static CharacterMatcher identifierStart() {
		return new CharacterMatcher() {
			@Override
			public boolean match(char ch) {
				return Character.isJavaIdentifierStart(ch);
			}

			@Override
			public String toString() {
			    return "match[identifierStart]";
			}
		};
	}

	public static CharacterMatcher identifierPart() {
		return new CharacterMatcher() {
			@Override
			public boolean match(char ch) {
				return Character.isJavaIdentifierPart(ch);
			}

			@Override
			public String toString() {
			    return "match[identifierPart]";
			}
		};
	}

	public static CharacterMatcher upper() {
		return new CharacterMatcher() {
			@Override
			public boolean match(char ch) {
				return Character.isUpperCase(ch);
			}

			@Override
			public String toString() {
			    return "match[uppercase]";
			}
		};
	}

	public static CharacterMatcher lower() {
		return new CharacterMatcher() {
			@Override
			public boolean match(char ch) {
				return Character.isLowerCase(ch);
			}

			@Override
			public String toString() {
			    return "match[lowercase]";
			}
		};
	}

	public static CharacterMatcher digit() {
		return new CharacterMatcher() {
			@Override
			public boolean match(char ch) {
				return Character.isDigit(ch);
			}

			@Override
			public String toString() {
			    return "match[digits]";
			}
		};
	}

	public static CharacterMatcher digit(final int radix) {
		return new CharacterMatcher() {
			@Override
			public boolean match(char ch) {
				return Character.digit(ch, radix) != -1;
			}

			@Override
			public String toString() {
			    return "match[digits of base " + radix + "]";
			}
		};
	}

	public static CharacterMatcher control() {
		return new CharacterMatcher() {
			@Override
			public boolean match(char ch) {
				return Character.isISOControl(ch);
			}

			@Override
			public String toString() {
				return "match[control]";
			}
		};
	}

	public static CharacterMatcher match(char ...chars) {
		final HashSet<Character> characters = new HashSet<Character>(chars.length);
		for (char ch : chars)
			characters.add(ch);
		return new CharacterMatcher() {
			@Override
            public boolean match(char ch) {
	            return characters.contains(ch);
            }

			@Override
			public String toString() {
				return "match[" + characters + "]";
			}
		};
	}
}
