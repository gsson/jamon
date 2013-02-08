package se.fnord.jamon.internal;

import se.fnord.jamon.Node;
import se.fnord.jamon.ParseContext;

public class ParseContextImpl implements ParseContext {

	private final char[] chars;
	private int start;

	ParseContextImpl(int start, char[] chars) {
		this.chars = chars;
		this.start = start;
	}

	@Override
	public int start() {
		return start;
	}

	@Override
	public int length() {
		return chars.length - start;
	}

	@Override
	public char charAt(int index) {
		return chars[start + index];
	}

	@Override
	public ParseContext splice(int index) {
		return new ParseContextImpl(index, chars, nodeCache);
	}

	@Override
    public Node node(int index, Object attachment) {
	    return new Node(start, start + index, new String(chars, start, index), attachment);
    }

	@Override
    public Node node(int index, String value, Object attachment) {
	    return new Node(start, start + index, value, attachment);
    }

	@Override
	public String toString() {
	    return new String(chars, start, Math.min(chars.length - start, 16));
	}
	
}
