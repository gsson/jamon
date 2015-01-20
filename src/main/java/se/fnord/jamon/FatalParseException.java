package se.fnord.jamon;

public class FatalParseException extends Exception {
    private static final long serialVersionUID = -3040764516082580027L;

    public FatalParseException() {
    	super();
    }

    public FatalParseException(String message) {
    	super(message);
    }
    public FatalParseException(String message, Throwable cause) {
    	super(message, cause);
    }

    public FatalParseException(Throwable cause) {
    	super(cause.getMessage(), cause);
    }
}
