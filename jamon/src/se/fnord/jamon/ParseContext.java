package se.fnord.jamon;

public interface ParseContext {
	int start();
	int length();
	char charAt(int index);
	ParseContext splice(int end);

	Node node(int index, Object attachment);
	Node node(int index, String value, Object attachment);

}
