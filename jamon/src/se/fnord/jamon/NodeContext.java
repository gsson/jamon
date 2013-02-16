package se.fnord.jamon;

public interface NodeContext {
	NodeContext forChild(Node n);
	NodeContext forParent();
	boolean matches(NodeMatcher matcher);
}
