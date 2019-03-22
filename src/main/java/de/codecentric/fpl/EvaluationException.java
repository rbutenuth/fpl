package de.codecentric.fpl;

import java.lang.reflect.InvocationTargetException;

/**
 * Problems during evaluation.
 */
public class EvaluationException extends Exception {
    private static final long serialVersionUID = 6161879115582780204L;
    private int added;

    /**
     * @param message Error message.
     */
    public EvaluationException(String message) {
        super(message);
    }

    /**
     * @param message Error message.
     * @param cause Root cause for this exception.
     */
    public EvaluationException(String message, Throwable cause) {
    	this(determineMessage(message, cause));
        initCause(cause);
    }

    private static String determineMessage(String message, Throwable cause) {
    	if (cause instanceof InvocationTargetException) {
    		return cause.getCause().getMessage();
    	} else {
    		if (message == null || message.length() == 0) {
    			return cause.toString();
    		} else {
    			return message;
    		}
    	}
    }
    
    /**
     * @param cause Root cause for this exception.
     */
    public EvaluationException(Throwable cause) {
        this(determineMessage(null, cause));
        initCause(cause);
    }

    @Override
    public Throwable initCause(Throwable cause) {
    	if (cause instanceof InvocationTargetException) {
    		cause = cause.getCause();
    	}
    	return super.initCause(cause);
    }
    
	public void add(StackTraceElement stackTraceElement) {
		StackTraceElement[] st = getStackTrace();
		StackTraceElement[] newSt = new StackTraceElement[st.length + 1];
		
		System.arraycopy(st, 0, newSt, 0, added);
		newSt[added] = stackTraceElement;
		System.arraycopy(st, added, newSt, added + 1, st.length - added);
		setStackTrace(newSt);
		added++;
	}
	
	/**
	 * @return Number of {@link StackTraceElement}s added by the FPL interpreter.
	 */
	public int getAdded() {
		return added;
	}
}
