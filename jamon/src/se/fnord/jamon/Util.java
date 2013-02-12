package se.fnord.jamon;

public class Util {
	/**
	 * Verify a parse tree
	 */
	static void assertMatch(NodeMatcher matcher, Node ... path) {
		if (!NodeMatchers.match(matcher, path))
			throw new AssertionError("NodeMatcher does not match the provided path");
	}
}
