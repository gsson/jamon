package se.fnord.jamon.internal;

import java.util.Arrays;

import se.fnord.jamon.Node;
import se.fnord.jamon.NodeContext;

public class NodeContextImpl implements NodeContext {
	private Node[] path;
	public NodeContextImpl(Node ... startPath) {
		checkPath(startPath, 0);
		this.path = startPath.clone();
    }

	private NodeContextImpl(Node[] head, Node tail) {
		if (!head[head.length - 1].children().contains(tail))
			throw new IllegalStateException("Provided node is not a child of the last node in the path");
		this.path = Arrays.copyOf(head, head.length + 1);
		this.path[this.path.length - 1] = tail;
    }

	private void checkPath(Node[] path, int start) {
		if (start == path.length)
			return;
		int next = start + 1;
		Node nextNode = path[next];
		if (path[start].children().contains(nextNode))
			checkPath(path, next);
		throw new IllegalStateException("Broken path");
	}
	
	@Override
    public NodeContext forChild(Node n) {
	    return new NodeContextImpl(path, n);
    }
}
