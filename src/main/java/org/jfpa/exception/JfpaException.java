package org.jfpa.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 02/10/11
 */
public class JfpaException extends RuntimeException {

    private static final String FATAL = "Jfpa Fatal Exception";

    public JfpaException(final String message) {
        super(FATAL + ": " + message);
    }

    public JfpaException(final Throwable cause) {
        super(FATAL, cause);
    }

    public JfpaException(final Class clazz, final Throwable cause) {
        super(clazz.getName() + ": " + FATAL, cause);
    }

    public JfpaException(final Class clazz, final String message) {
        super(clazz.getName() + ": " + message);
    }
}
