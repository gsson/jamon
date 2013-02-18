package se.fnord.jamon;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import se.fnord.jamon.internal.Contexts;
import se.fnord.jamon.internal.PathStack;

/**
 * Utility to verify parse trees
 */
public class NodeMatchers {
	private static final class Equality<T> implements Predicate<T> {
		private final T reference;

		public Equality(T reference) {
			this.reference = reference;
		}

		@Override
        public boolean test(T value) {
	        return Objects.equals(reference, value);
        }

		@Override
		public int hashCode() {
		    return 16 + 31 * Objects.hashCode(reference);
		}

		@Override
		public boolean equals(Object obj) {
			return (obj == this) || (obj instanceof Equality) && Objects.equals(reference, ((Equality<?>) obj).reference);
		}
	}

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
			for (Node c : n.children()) {
				if (!context.forChild(c).matches(childMatcher))
					return false;
			}
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

	private static final class ForParent implements NodeMatcher {
		private final NodeMatcher parentMatcher;

		public ForParent(NodeMatcher parentMatcher) {
			this.parentMatcher = parentMatcher;
        }

		@Override
		public boolean match(NodeContext context, Node n) {
			return context.forParent().matches(parentMatcher);
		}

		@Override
		public int hashCode() {
		    return 3 + 31 * parentMatcher.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return (obj == this) || (obj instanceof ForParent) && Objects.equals(parentMatcher, ((ForParent) obj).parentMatcher);
		}
	}

	private static final class Attachment implements NodeMatcher {
		private final Predicate<Object> tester;

		public Attachment(Predicate<Object> tester) {
			this.tester = tester;
        }

		@Override
		public boolean match(NodeContext context, Node n) {
			return tester.test(n.attachment());
		}

		@Override
		public int hashCode() {
		    return 4 + 31 * Objects.hashCode(tester);
		}

		@Override
		public boolean equals(Object obj) {
			return (obj == this) || (obj instanceof Attachment) && Objects.equals(tester, ((Attachment) obj).tester);
		}
	}

	private static final class Value implements NodeMatcher {
		private final Predicate<String> tester;

		public Value(Predicate<String> tester) {
			this.tester = tester;
        }

		@Override
		public boolean match(NodeContext context, Node n) {
			return tester.test(n.value());
		}

		@Override
		public int hashCode() {
		    return 5 + 31 * Objects.hashCode(tester);
		}

		@Override
		public boolean equals(Object obj) {
			return (obj == this) || (obj instanceof Value) && Objects.equals(tester, ((Value) obj).tester);
		}
	}

	private static final class Children implements NodeMatcher {
		private final NodeMatcher[] matchers;

		public Children(NodeMatcher[] matchers) {
			this.matchers = matchers;
        }

		@Override
		public boolean match(NodeContext context, Node n) {
			final List<Node> children = n.children();
			final int childCount = children.size();

			if (matchers.length != childCount)
				return false;
			for (int i = 0; i < childCount; i++) {
				Node child = children.get(i);
				if (!context.forChild(child).matches(matchers[i]))
					return false;
			}
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
				if (!context.matches(v))
					return false;
			return true;
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
				if (context.matches(v))
					return true;
			return false;
		}

		@Override
		public int hashCode() {
			int n = 8;
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
	 * Verifies the parent against the supplied NodeMatcher
	 * @param matcher The matcher to check the parent against
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher forParent(final NodeMatcher matcher) {
		return new ForParent(matcher);
	}

	/**
	 * Verifies the node attachment
	 * @param o The object to check for equality with the node attachment
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher attachment(final Object o) {
		return testAttachment(new Equality<>(o));
	}

	/**
	 * Verifies the node attachment
	 * @param tester The predicate to use when testing the attachment value
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher testAttachment(final Predicate<Object> tester) {
		return new Attachment(tester);
	}

	/**
	 * Verifies the node value
	 * @param o The object to check for equality with the node value
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher value(final String s) {
		return testValue(new Equality<>(s));
	}

	/**
	 * Verifies the node value
	 * @param o The object to check for equality with the node value
	 * @return the constructed NodeMatcher
	 */
	public static NodeMatcher testValue(final Predicate<String> tester) {
		return new Value(tester);
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
	public static NodeMatcher node(final Object attachment, final String value, final NodeMatcher ... children) {
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
		final NodeContext context = new Contexts().nodeContext(path);
		return context.matches(matcher);
	}

	private static class MatchFirstVisitor implements PathVisitor {
		private final Contexts contextFactory;
		private Path result;
		private NodeMatcher matcher;

		public MatchFirstVisitor(Contexts contextFactory, NodeMatcher matcher) {
			this.contextFactory = contextFactory;
			this.matcher = matcher;
        }

		@Override
        public boolean visit(Path path) {
			if (contextFactory.nodeContext(path).matches(matcher)) {
				result = path;
				return false;
			}
			return true;
        }

		public Path result() {
	        return result;
        }
	}

	public static void traverseBreadthFirst(Path path, PathVisitor visitor) {
		if (!visitor.visit(path))
			return;

		PathStack stack = new PathStack();
		stack.append(path, path.leaf().children());
		while (!stack.isEmpty()) {
			path = stack.poll();
			if (!visitor.visit(path))
				return;

			stack.append(path, path.leaf().children());
		}
	}

	public static void traverseDepthFirst(Path path, PathVisitor visitor) {
		if (!visitor.visit(path))
			return;

		PathStack stack = new PathStack();
		stack.prepend(path, path.leaf().children());
		while (!stack.isEmpty()) {
			System.err.println(stack);
			path = stack.poll();
			if (!visitor.visit(path))
				return;

			stack.prepend(path, path.leaf().children());
		}
	}

	/**
	 * Find the first node matching the matcher using a depth first search.
	 * @param matcher matcher to execute
	 * @param path Nodes describing the path to the root of the sub-tree to search.
	 * @return the first matching node found.
	 */
	public static Path findFirstDF(NodeMatcher matcher, Path path) {
		MatchFirstVisitor visitor = new MatchFirstVisitor(new Contexts(), matcher);
		traverseDepthFirst(path, visitor);
		return visitor.result();
	}

	/**
	 * Find the first node matching the matcher using a breadth first search.
	 * @param matcher matcher to execute
	 * @param path Nodes describing the path to the root of the sub-tree to search.
	 * @return the first matching node found.
	 */
	public static Path findFirstBF(NodeMatcher matcher, Path path) {
		MatchFirstVisitor visitor = new MatchFirstVisitor(new Contexts(), matcher);
		traverseBreadthFirst(path, visitor);
		return visitor.result();
	}
}
