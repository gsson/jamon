package se.fnord.jamon.internal;

import java.util.HashMap;
import java.util.Map;

import se.fnord.jamon.Node;
import se.fnord.jamon.NodeContext;
import se.fnord.jamon.ParseContext;
import se.fnord.jamon.Path;

public class Contexts {
	private final Map<ParseContextImpl.CacheKey, Node> parseCache = new HashMap<>();
	private final Map<NodeContextImpl.CacheKey, Boolean> matchCache = new HashMap<>();

	public ParseContext parseContext(String input) {
		return new ParseContextImpl(0, input.toCharArray(), parseCache);
	}

	public NodeContext nodeContext(Path path) {
		return new NodeContextImpl(path, matchCache);
	}
}
