package se.fnord.jamon.internal;

import se.fnord.jamon.ParseContext;

public class Contexts {
	public static ParseContext create(String input) {
		return new ParseContextImpl(0, input.toCharArray());
	}
}
