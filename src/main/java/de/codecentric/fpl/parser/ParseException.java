package de.codecentric.fpl.parser;

/**
 * Scanner or parser problems, always contains information about the {@link Position} of problem.
 */
public class ParseException extends RuntimeException {
    private static final long serialVersionUID = -5654631491269471825L;
    private Position position;

    /**
     * @param position Where the error ocurred.
     * @param message Error message.
     */
    public ParseException(Position position, String message) {
        super(message);
        if (position != null) {
            this.position = position;
        } else {
            // Better an invalid position as an error while reporting an error...
            this.position = Position.UNKNOWN;
        }
    }

    /**
     * @param position Where the error ocurred.
     * @param message Error message.
     * @param cause Root cause for thie exception.
     */
    public ParseException(Position position, String message, Throwable cause) {
        this(position, message);
        initCause(cause);
    }

    /**
     * @return Position in source code.
     */
    public Position getPosition() {
        return position;
    }
}
