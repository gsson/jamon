package se.fnord.jamon;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class Nodes {
	private final Map<NodeBuilder, Node> buildCache = new IdentityHashMap<>();

	public class NodeBuilder {
		private NodeBuilder[] children = new NodeBuilder[0];
		private String value;
		private AttachmentFactory attachmentFactory = new Parsers.StaticAttachmentFactory(null);

		public NodeBuilder attachment(Object attachment) {
			this.attachmentFactory = new Parsers.StaticAttachmentFactory(attachment);
			return this;
		}

		public NodeBuilder attachmentFactory(AttachmentFactory factory) {
			this.attachmentFactory = factory;
			return this;
		}

		public NodeBuilder value(String value) {
			this.value = value;
			return this;
		}

		public NodeBuilder children(NodeBuilder... children) {
			this.children = children.clone();
			return this;
		}

		private List<Node> buildChildren() {
			final List<Node> list = new ArrayList<>(children.length);
			for (NodeBuilder c : children)
				list.add(c.build());
			return list;
		}

		public Node build() {
			Node node = buildCache.get(this);
			if (node != null)
				return node;
			List<Node> childrenList = buildChildren();
			node = new Node(value, attachmentFactory.create(value, childrenList)).addChildren(childrenList);
			Node oldNode = buildCache.put(this, node);
			if (oldNode != null && oldNode != node)
				throw new IllegalStateException("Inconsistent node creation");
			return node;
		}
	}

	public NodeBuilder node() {
		return new NodeBuilder();
	}

	public NodeBuilder node(String value, Object attachment) {
		return node().value(value).attachment(attachment);
	}

	public NodeBuilder node(String value) {
		return node().value(value);
	}

	public NodeBuilder node(String value, NodeBuilder... children) {
		return node().value(value).children(children);
	}

	public NodeBuilder node(String value, Object attachment, NodeBuilder... children) {
		return node().value(value).attachment(attachment).children(children);
	}
}
