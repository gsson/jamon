package se.fnord.jamon;

import se.fnord.jamon.internal.Contexts;

/**
 * Utility to verify parse trees
 */
public class NodeMatchers {
	/**
	 * Verifies the number of children
	 * @param count The number of children
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher childCount(final int count) {
		return new NodeMatcher() {
			@Override
			public boolean match(NodeContext context, Node n) {
				return count == n.children().size();
			}
		};		
	}

	/**
	 * Verifies all children against the supplied NodeMatcher
	 * @param matcher The matcher to run for each child
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher forEachChild(final NodeMatcher matcher) {
		return new NodeMatcher() {
			@Override
			public boolean match(NodeContext context, Node n) {
				for (Node c : n.children())
					if (!matcher.match(context, c))
						return false;
				return true;
			}
		};		
	}

	/**
	 * Verifies the node attachment
	 * @param o The object to check for equality with the node attachment
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher attachment(final Object o) {
		return new NodeMatcher() {
			@Override
			public boolean match(NodeContext context, Node n) {
				if (o == n.attachment())
					return true;
				if (o != null && o.equals(n.attachment()))
					return true;
				return false;
			}
		};
	}

	/**
	 * Verifies the node value
	 * @param o The object to check for equality with the node value
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher value(final Object o) {
		return new NodeMatcher() {
			@Override
			public boolean match(NodeContext context, Node n) {
				if (o == n.value())
					return true;
				if (o != null && o.equals(n.value()))
					return true;
				return false;
			}
		};
	}

	/**
	 * Verifies the child nodes. The number of children must be equal to the number of supplied NodeMatchers.
	 * @param matchers The matchers to use when verifying children
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher children(final NodeMatcher ... matchers) {
		return new NodeMatcher() {
			@Override
			public boolean match(NodeContext context, Node n) {
				if (matchers.length != n.children().size())
					return false;
				int i = 0;
				for (Node c : n.children())
					if (!matchers[i++].match(context, c))
						return false;
				return true;
			}
		};
	}

	/**
	 * The conjunction of zero or more NodeMatchers. Passing no NodeMatchers creates a NodeMatcher that always return true.
	 * @param matchers The matchers whose results should be and:ed together
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher and(final NodeMatcher ... matchers) {
		return new NodeMatcher() {
			@Override
			public boolean match(NodeContext context, Node n) {
				for (NodeMatcher v : matchers)
					if (!v.match(context, n))
						return false;
				return true;
			}
		};
	}

	/**
	 * The disjunction of zero or more NodeMatchers. Passing no matchers creates a matcher that always return false.
	 * @param matchers The matchers whose results should be or:ed together
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher or(final NodeMatcher ... matchers) {
		return new NodeMatcher() {
			@Override
			public boolean match(NodeContext context, Node n) {
				for (NodeMatcher v : matchers)
					if (v.match(context, n))
						return true;
				return false;
			}
		};
	}

	/**
	 * Verifies the attachment, value and children of a node.
	 * Equivalent to:
 	 * <p>
	 * <code>and(attachment(attachment), value(value), children(children))</code>
	 * <p>
	 * @param attachment The object to check for equality with the node attachment
	 * @param value The object to check for equality with the node value
	 * @param matchers The matchers to use when verifying children
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher node(final Object attachment, final Object value, final NodeMatcher ... children) {
		return and(attachment(attachment), value(value), children(children));
	}

	/**
	 * Verifies that a node is empty, i.e. has no value, attachment or children
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher empty() {
		return and(value(null), attachment(null), childCount(0));
	}

	/**
	 * Execute a node matcher on a Node path
	 * @param matcher matcher to execute
	 * @param path Nodes describing the path to the node where the matcher should start.
	 * @return true if the matcher succeeds, false otherwise.
	 */
	public static boolean match(NodeMatcher matcher, Path path) {
		final NodeContext context = Contexts.nodeContext(path);
		return matcher.match(context, path.leaf());
	}
}
