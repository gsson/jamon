package se.fnord.jamon;

import java.util.Arrays;
import java.util.Objects;

import se.fnord.jamon.internal.Contexts;

/**
 * Utility to verify parse trees
 */
public class NodeMatchers {
	private static final class ChildCount implements NodeMatcher {
		private final int count;

		public ChildCount(int count) {
			this.count = count;
        }

		@Override
		public boolean match(NodeContext context, Node n) {
			return count == n.children().size();
		}

		@Override
		public int hashCode() {
		    return 1 + 31 * count;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == this)
				return true;
			return (obj == this) || (obj instanceof ChildCount) && count == ((ChildCount) obj).count;
		}
	}

	private static final class ForEachChild implements NodeMatcher {
		private final NodeMatcher childMatcher;

		public ForEachChild(NodeMatcher childMatcher) {
			this.childMatcher = childMatcher;
        }

		@Override
		public boolean match(NodeContext context, Node n) {
			for (Node c : n.children())
				if (!childMatcher.match(context, c))
					return false;
			return true;
		}

		@Override
		public int hashCode() {
		    return 2 + 31 * childMatcher.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return (obj == this) || (obj instanceof ForEachChild) && Objects.equals(childMatcher, ((ForEachChild) obj).childMatcher);
		}
	}

	private static final class Attachment implements NodeMatcher {
		private final Object attachment;

		public Attachment(Object attachment) {
			this.attachment = attachment;
        }

		@Override
		public boolean match(NodeContext context, Node n) {
			return Objects.equals(attachment, n.attachment());
		}

		@Override
		public int hashCode() {
		    return 3 + 31 * Objects.hashCode(attachment);
		}

		@Override
		public boolean equals(Object obj) {
			return (obj == this) || (obj instanceof Attachment) && Objects.equals(attachment, ((Attachment) obj).attachment);
		}
	}

	private static final class Value implements NodeMatcher {
		private final Object value;

		public Value(Object value) {
			this.value = value;
        }

		@Override
		public boolean match(NodeContext context, Node n) {
			return Objects.equals(value, n.value());
		}

		@Override
		public int hashCode() {
		    return 4 + 31 * Objects.hashCode(value);
		}

		@Override
		public boolean equals(Object obj) {
			return (obj == this) || (obj instanceof Value) && Objects.equals(value, ((Value) obj).value);
		}
	}

	private static final class Children implements NodeMatcher {
		private final NodeMatcher[] matchers;

		public Children(NodeMatcher[] matchers) {
			this.matchers = matchers;
        }

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

		@Override
		public int hashCode() {
			int n = 5;
			for (NodeMatcher m : matchers)
				n = n + 31 * m.hashCode();
		    return n;
		}

		@Override
		public boolean equals(Object obj) {
			return (obj == this) || (obj instanceof Children) && Arrays.equals(matchers, ((Children) obj).matchers);
		}
	}

	private static final class And implements NodeMatcher {
		private final NodeMatcher[] matchers;

		public And(NodeMatcher[] matchers) {
			this.matchers = matchers;
        }

		@Override
		public boolean match(NodeContext context, Node n) {
			for (NodeMatcher v : matchers)
				if (!v.match(context, n))
					return false;
			return true;
		}

		@Override
		public int hashCode() {
			int n = 6;
			for (NodeMatcher m : matchers)
				n = n + 31 * m.hashCode();
		    return n;
		}

		@Override
		public boolean equals(Object obj) {
			return (obj == this) || (obj instanceof And) && Arrays.equals(matchers, ((And) obj).matchers);
		}
	}

	private static final class Or implements NodeMatcher {
		private final NodeMatcher[] matchers;

		public Or(NodeMatcher[] matchers) {
			this.matchers = matchers;
        }

		@Override
		public boolean match(NodeContext context, Node n) {
			for (NodeMatcher v : matchers)
				if (v.match(context, n))
					return true;
			return false;
		}

		@Override
		public int hashCode() {
			int n = 7;
			for (NodeMatcher m : matchers)
				n = n + 31 * m.hashCode();
		    return n;
		}

		@Override
		public boolean equals(Object obj) {
			return (obj == this) || (obj instanceof Or) && Arrays.equals(matchers, ((Or) obj).matchers);
		}
	}

	/**
	 * Verifies the number of children
	 * @param count The number of children
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher childCount(final int count) {
		return new ChildCount(count);
	}

	/**
	 * Verifies all children against the supplied NodeMatcher
	 * @param matcher The matcher to run for each child
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher forEachChild(final NodeMatcher matcher) {
		return new ForEachChild(matcher);
	}

	/**
	 * Verifies the node attachment
	 * @param o The object to check for equality with the node attachment
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher attachment(final Object o) {
		return new Attachment(o);
	}

	/**
	 * Verifies the node value
	 * @param o The object to check for equality with the node value
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher value(final Object o) {
		return new Value(o);
	}

	/**
	 * Verifies the child nodes. The number of children must be equal to the number of supplied NodeMatchers.
	 * @param matchers The matchers to use when verifying children
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher children(final NodeMatcher ... matchers) {
		return new Children(matchers);
	}

	/**
	 * The conjunction of zero or more NodeMatchers. Passing no NodeMatchers creates a NodeMatcher that always return true.
	 * @param matchers The matchers whose results should be and:ed together
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher and(final NodeMatcher ... matchers) {
		return new And(matchers);
	}

	/**
	 * The disjunction of zero or more NodeMatchers. Passing no matchers creates a matcher that always return false.
	 * @param matchers The matchers whose results should be or:ed together
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher or(final NodeMatcher ... matchers) {
		return new Or(matchers);
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
