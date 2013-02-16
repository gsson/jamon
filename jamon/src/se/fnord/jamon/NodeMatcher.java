package se.fnord.jamon;

/**
 * A node matcher
 */
public interface NodeMatcher {
	boolean match(NodeContext context, Node n);
}
