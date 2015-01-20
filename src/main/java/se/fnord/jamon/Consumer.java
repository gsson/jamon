package se.fnord.jamon;

public interface Consumer {
	ParseContext consume(ParseContext input, Node parent) throws ParseException, FatalParseException;
}
