package se.fnord.jamon;

public class Util {
	private Util() {
		throw new IllegalAccessError("No Util for you!");
	}

	/**
	 * Verify a parse tree
	 */
	public static void assertMatches(NodeMatcher matcher, Node ... path) {
		if (!NodeMatchers.match(matcher, Path.path(path)))
			throw new AssertionError("NodeMatcher does not match the provided path");
	}
}
