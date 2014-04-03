package se.fnord.jamon;

public interface NodeContext {
	NodeContext forChild(Node n);
	NodeContext forParent();
	Path path();
	boolean matches(NodeMatcher matcher);
}
