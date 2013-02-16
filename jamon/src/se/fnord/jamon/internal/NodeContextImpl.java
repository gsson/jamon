package se.fnord.jamon.internal;

import se.fnord.jamon.Node;
import se.fnord.jamon.NodeContext;
import se.fnord.jamon.Path;

public class NodeContextImpl implements NodeContext {
	private Path path;
	public NodeContextImpl(Path path) {
		this.path = path;
    }
	
	@Override
    public NodeContext forChild(Node n) {
	    return new NodeContextImpl(path.forChild(n));
    }

	@Override
    public NodeContext forParent() {
	    return new NodeContextImpl(path.forParent());
    }
}
