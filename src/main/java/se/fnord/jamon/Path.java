package se.fnord.jamon;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class Path implements Iterable<Node> {
	private final Node[] path;

	private Path(Node[] path) {
		this.path = path;
    }

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof Path))
			return false;
		return elementsEquals((Path) obj);
	}

	private boolean elementsEquals(Path other) {
		if (path.length != other.path.length)
			return false;
		for (int i = 0; i < path.length; i++)
			if (!path[i].shallowEquals(other.path[i]))
				return false;
		return true;
	}

	@Override
	public int hashCode() {
		int n = 0;
		for (int i = 0; i < path.length; i++)
			n = n + 31 * path[i].hashCode();
		return n;
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

	public Path forParent() {
		final Node[] newPath = Arrays.copyOf(path, path.length - 1);
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
    	final Node[] newNodes = nodes.clone();
    	checkPath(newNodes);
    	return new Path(newNodes);
    }

    @Override
    public String toString() {
    	StringBuilder sb = new StringBuilder();
    	sb.append(path[0].value());
    	for (int i = 1; i < path.length; i++) {
    		sb.append("/").append(path[i].value());
    	}
    	return sb.toString();
    }

	@Override
    public Iterator<Node> iterator() {
	    return new Iterator<Node>() {
	    	private int i = 0;
			@Override
            public boolean hasNext() {
	            return i < path.length;
            }

			@Override
            public Node next() {
				if (i == path.length)
					throw new NoSuchElementException();
	            return path[i++];
            }

			@Override
            public void remove() {
				throw new UnsupportedOperationException();
            }
	    };
    }
}
