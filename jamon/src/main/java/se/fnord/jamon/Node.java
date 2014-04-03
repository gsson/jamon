package se.fnord.jamon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

public class Node {
	public static final int UNSET = -1;
	private final List<Node> children = new ArrayList<Node>();
	private final String value;
	private Object attachment;
	private int end;
	private int start;

	public Node(Object attachment) {
		this.start = UNSET;
		this.end = UNSET;
		this.value = null;
		this.attachment = attachment;
	}

	public Node() {
		this.start = UNSET;
		this.end = UNSET;
		this.value = null;
	}

	public Node(String value, Object attachment) {
		this.start = UNSET;
		this.end = UNSET;
		this.value = value;
		this.attachment = attachment;
	}

	public Node(int start, int end, String value, Object attachment) {
		this.start = start;
		this.end = end;
		this.value = value;
		this.attachment = attachment;
	}

	public Node(int start, int end, Object attachment) {
		this.start = start;
		this.end = end;
		this.value = null;
		this.attachment = attachment;
	}

	public int start() {
		return start;
	}

	void start(int start) {
		this.start = start;
	}

	public int end() {
		return end;
	}

	void end(int end) {
		this.end = end;
	}

	void attachment(Object attachment) {
		this.attachment = attachment;
	}

	Node addChildren(Node ... nodes) {
		for (final Node node : nodes)
			children.add(node);
		return this;
	}

	Node addChildren(List<Node> nodes) {
		children.addAll(nodes);
		return this;
	}

	public List<Node> children() {
		return Collections.unmodifiableList(children);
	}

	public Node firstChild() {
		return children.get(0);
	}

	public String value() {
		return value;
	}

	public Object attachment() {
		return attachment;
	}

	@Override
	public boolean equals(Object obj) {
		return shallowEquals(obj) && Objects.equals(children, ((Node) obj).children);
	}

	public boolean shallowEquals(Object obj) {
		if (!(obj instanceof Node))
			return false;
		final Node other = (Node) obj;
		return (start == other.start) && (end == other.end) && Objects.equals(value, other.value) && Objects.equals(attachment, other.attachment);
	}

	@Override
	public int hashCode() {
		return (value == null ? 0 : value.hashCode()) + 31 * (attachment == null ? 0 : attachment.hashCode());
	}

	private static String toString(List<Node> nodes) {
		if (nodes.isEmpty())
			return "{}";

		final StringBuilder sb = new StringBuilder();

		final Iterator<Node> i = nodes.iterator();
		final Node first = i.next();
		sb.append("{").append(first.toString());
		while (i.hasNext())
			sb.append(", ").append(i.next().toString());
		return sb.append("}").toString();
	}

	private static String toPrefixedString(String prefix, List<Node> nodes) {
		if (nodes.isEmpty())
			return "{}";

		final StringBuilder sb = new StringBuilder();

		final Iterator<Node> i = nodes.iterator();
		final Node first = i.next();
		sb.append("{\n").append(prefix).append(first.dump(prefix + "  "));
		while (i.hasNext())
			sb.append(",\n").append(prefix).append(i.next().dump(prefix + "  "));
		return sb.append("}").toString();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(String.format("N[(%d, %d)", start, end));
		if (attachment != null)
			sb.append(", attach=").append(attachment);
		if (value != null)
			sb.append(", val=\"").append(value).append("\"");
		if (!children.isEmpty())
			sb.append(", child=").append(toString(children));

		return sb.append("]").toString();
	}

	private String dump(String prefix) {
		StringBuilder sb = new StringBuilder("Node[");
		if (attachment != null)
			sb.append("attachment=").append(attachment);
		if (attachment != null && value != null)
			sb.append(", ");
		if (value != null)
			sb.append("value=\"").append(value).append("\"");
		if ((value != null || attachment != null) && !children.isEmpty())
			sb.append(", ");
		if (!children.isEmpty())
			sb.append("children=").append(toPrefixedString(prefix + "  ", children));

		return sb.append("]").toString();

	}

	public String dump() {
		return dump("");
	}
}
