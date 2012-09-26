package se.fnord.jamon;

import java.util.HashSet;

public class Matchers {

	public static Matcher not(final Matcher matcher) {
		return new Matcher() {
			@Override
            public boolean match(char ch) {
				return !matcher.match(ch);
            }
		};
	}

	public static Matcher not(char ... chars) {
		return not(match(chars));
	}

	public static Matcher and(final Matcher ... matchers) {
		return new Matcher() {
			@Override
            public boolean match(char ch) {
				for (Matcher matcher : matchers)
					if (!matcher.match(ch))
						return false;
				return true;
            }
		};
	}

	public static Matcher or(final Matcher ... matchers) {
		return new Matcher() {
			@Override
            public boolean match(char ch) {
				for (Matcher matcher : matchers)
					if (matcher.match(ch))
						return true;
				return false;
            }
		};
	}

	public static Matcher newline() {
		return match('\r', '\n');
	}

	public static Matcher letter() {
		return new Matcher() {
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

	public static Matcher white() {
		return new Matcher() {
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

	public static Matcher identifierStart() {
		return new Matcher() {
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

	public static Matcher identifierPart() {
		return new Matcher() {
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

	public static Matcher upper() {
		return new Matcher() {
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

	public static Matcher lower() {
		return new Matcher() {
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

	public static Matcher digit() {
		return new Matcher() {
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

	public static Matcher digit(final int radix) {
		return new Matcher() {
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

	public static Matcher control() {
		return new Matcher() {
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

	public static Matcher match(char ...chars) {
		final HashSet<Character> characters = new HashSet<Character>(chars.length);
		for (char ch : chars)
			characters.add(ch);
		return new Matcher() {
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
