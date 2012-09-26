package se.fnord.jamon;

public class ParseException extends Exception {
    private static final long serialVersionUID = -3561691790478317477L;

    public ParseException() {
    	super();
    }

    public ParseException(String message) {
    	super(message);
    }
    public ParseException(String message, Throwable cause) {
    	super(message, cause);
    }
}
