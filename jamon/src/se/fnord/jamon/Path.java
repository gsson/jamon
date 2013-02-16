package se.fnord.jamon;

import java.util.Arrays;

public class Path {
	private final Node[] path;

	private Path(Node[] path) {
		if (path.length == 0)
			throw new IllegalStateException("Broken path");
		else if (path.length > 1)
			checkPath(path, 0);
		this.path = path.clone();
    }

	private Path(Node[] head, Node tail) {
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

	public Node root() {
		return path[0];
	}

	public Node leaf() {
		return path[path.length - 1];
	}
	
    public Path forChild(Node n) {
	    return new Path(path, n);
    }
    
    public static Path path(Node ...nodes) {
    	return new Path(nodes);
    }
}
