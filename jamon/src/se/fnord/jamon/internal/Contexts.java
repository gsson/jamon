package se.fnord.jamon.internal;

import se.fnord.jamon.Node;
import se.fnord.jamon.NodeContext;
import se.fnord.jamon.ParseContext;

public class Contexts {
	public static ParseContext parseContext(String input) {
		return new ParseContextImpl(0, input.toCharArray());
	}
	
	public static NodeContext nodeContext(Node...path) {
		return new NodeContextImpl(path);
	}
}
