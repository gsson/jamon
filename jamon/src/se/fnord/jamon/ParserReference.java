package se.fnord.jamon;

public interface ParserReference extends Parser {
	void setTarget(Consumer parser);
}
