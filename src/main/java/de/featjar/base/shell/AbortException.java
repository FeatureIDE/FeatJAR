package de.featjar.base.shell;

public class AbortException extends Exception {

    private static final long serialVersionUID = 7000673448692577055L;

    public AbortException() {
        super();
    }

    public AbortException(String message) {
        super(message);
    }

    public AbortException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public AbortException(String message, Throwable cause) {
        super(message, cause);
    }

    public AbortException(Throwable cause) {
        super(cause);
    }
}
