package se.fnord.jamon.internal;

import se.fnord.jamon.NodeContext;
import se.fnord.jamon.ParseContext;
import se.fnord.jamon.Path;

public class Contexts {
	public static ParseContext parseContext(String input) {
		return new ParseContextImpl(0, input.toCharArray());
	}
	
	public static NodeContext nodeContext(Path path) {
		return new NodeContextImpl(path);
	}
}
