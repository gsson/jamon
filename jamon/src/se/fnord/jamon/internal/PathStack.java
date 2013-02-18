package se.fnord.jamon.internal;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;

import se.fnord.jamon.Node;
import se.fnord.jamon.Path;

public class PathStack {
	private static final Holder EMPTY = new Holder(null, new ArrayDeque<Node>(0));

	private static final class Holder {
		public final Path parent;
		public final Deque<Node> children;

		public Holder(Path parent, Deque<Node> children) {
			this.parent = parent;
			this.children = children;
		}

		public void buildString(StringBuilder sb) {
			sb.append("[");
			Iterator<Node> iterator = children.iterator();
			Node n;
			if (iterator.hasNext()) {
				n = iterator.next();
				sb.append(parent.forChild(n).toString());
				while (iterator.hasNext()) {
					n = iterator.next();
					sb.append(", ").append(parent.forChild(n).toString());
				}
			}
			sb.append("]");
		}
	}

	private final Deque<Holder> queue = new ArrayDeque<>();
	private Holder current = EMPTY;

	public void prepend(Path parent, Collection<Node> children) {
		if (!children.isEmpty()) {
			if (!current.children.isEmpty())
				queue.addFirst(current);
			current = new Holder(parent, new ArrayDeque<>(children));
		}
	}

	public void append(Path parent, Collection<Node> children) {
		if (!children.isEmpty()) {
			if (queue.isEmpty() && current.children.isEmpty())
				current = new Holder(parent, new ArrayDeque<>(children));
			else
				queue.addLast(new Holder(parent, new ArrayDeque<>(children)));
		}
	}
	
	public Path poll() {
		Node c = current.children.poll();
		if (c != null)
			return current.parent.forChild(c);
		Holder next = queue.poll();
		if (next == null) {
			current = EMPTY;
			return null;
		}
		current = next;
		c = current.children.poll();
		return current.parent.forChild(c);
	}

	public boolean isEmpty() {
		return current.children.isEmpty() && queue.isEmpty();
	}
	
	@Override
	public String toString() {
		StringBuilder b = new StringBuilder();
		b.append("PathStack[");
		current.buildString(b);
		for (Holder h : queue) {
			b.append(", ");
			h.buildString(b);
		}
		b.append("]");
		return b.toString();
	}
}
