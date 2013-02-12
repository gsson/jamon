package se.fnord.jamon.internal;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import se.fnord.jamon.Consumer;
import se.fnord.jamon.Node;
import se.fnord.jamon.ParseContext;
import se.fnord.jamon.ParseException;

public class ParseContextImpl implements ParseContext {
	private static final Node NONMATCHING = new Node();

	public final class CacheKey {
		private final int start;
		private final Consumer group;

		public CacheKey(int start, Consumer group) {
			this.start = start;
			this.group = group;
		}

		public final int start() {
			return start;
		}

		public final Consumer group() {
			return group;
		}

		@Override
		public final String toString() {
			return String.format("{ %d, %s }", start, Objects.toString(group));
		}

		@Override
		public int hashCode() {
			return start * 31 + System.identityHashCode(group);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (!(obj instanceof CacheKey))
				return false;
			final CacheKey that = (CacheKey) obj;
			return this.start == that.start && this.group == that.group; // Yes, we really do want instance checks.
		}
	}

	private final Map<CacheKey, Node> nodeCache;
	private final char[] chars;
	private final int start;
	private final int end;

	ParseContextImpl(int start, char[] chars) {
		this(start, chars, new HashMap<CacheKey, Node>());
	}

	ParseContextImpl(int start, char[] chars, Map<CacheKey, Node> nodeCache) {
		this(start, chars.length, chars, new HashMap<CacheKey, Node>());
	}

	ParseContextImpl(int start, int end, char[] chars, Map<CacheKey, Node> nodeCache) {
		this.chars = chars;
		this.start = start;
		this.end = end;
		this.nodeCache = nodeCache;
	}

	public Node consumerMatched(Consumer group) throws ParseException {
		Node n = nodeCache.get(new CacheKey(start, group));
		if (n == NONMATCHING)
			throw new ParseException();
		return n;
	}

	public Node consumerMatches(Consumer group, Node node) {
		CacheKey key = new CacheKey(start, group);
		Node oldNode = nodeCache.get(key);
		if (oldNode != null) {
			if (!oldNode.equals(node))
				throw new IllegalStateException("The (start, parser) yielded different results on different rounds");
			return oldNode;
		}
		nodeCache.put(key, node);
		return node;
	}

	public void consumerMismatches(Consumer group) {
		Node oldNode = nodeCache.put(new CacheKey(start, group), NONMATCHING);
		if (oldNode != null && oldNode != NONMATCHING)
			throw new IllegalStateException("The (start, parser) yielded different results on different rounds");
	}

	@Override
	public int start() {
		return start;
	}

	@Override
	public int end() {
		return end;
	}

	@Override
	public int length() {
		return end - start;
	}

	@Override
	public char charAt(int index) {
		if (index >= end)
			throw new IndexOutOfBoundsException();
		return chars[start + index];
	}

	@Override
	public ParseContext splice(int splicePoint) {
		return new ParseContextImpl(splicePoint, end, chars, nodeCache);
	}

	@Override
	public ParseContext splice(int splicePoint, int end) {
		return new ParseContextImpl(splicePoint, end, chars, nodeCache);
	}

	@Override
    public Node node(int index, Object attachment) {
	    return new Node(start, start + index, new String(chars, start, index), attachment);
    }

	@Override
    public Node node(int index, String value, Object attachment) {
	    return new Node(start, start + index, value, attachment);
    }

	@Override
	public String toString() {
	    return new String(chars, start, Math.min(chars.length - start, 16));
	}
	
}
