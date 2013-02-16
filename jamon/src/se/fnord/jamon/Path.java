package se.fnord.jamon;

import java.util.Arrays;

public class Path {
	private final Node[] path;

	private Path(Node[] path) {
		this.path = path;
    }


	}

	public Node root() {
		return path[0];
	}

	public Node leaf() {
		return path[path.length - 1];
	}

	public Path forChild(Node n) {
		if (!leaf().children().contains(n))
			throw new IllegalStateException("Provided node is not a child of the last node in the path");

		final Node[] newPath = Arrays.copyOf(path, path.length + 1);
		newPath[newPath.length - 1] = n;
		return new Path(newPath);
	}


	private static void checkPath(Node[] path) {
		if (path.length == 0)
			throw new IllegalStateException("Broken path");

		Node parent = path[0];
		for (int i = 1; i < path.length; i++) {
			Node current = path[i];
			if (!parent.children().contains(current))
				throw new IllegalStateException("Broken path");
			parent = current;
		}
	}

    public static Path path(Node ...nodes) {
    	nodes = nodes.clone();
    	checkPath(nodes);
    	return new Path(nodes);
    }
}
