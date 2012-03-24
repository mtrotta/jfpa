package org.jfpa.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 22/03/11
 */
public class InvalidMultipleRecordException extends InvalidRecordException {

    private static final long serialVersionUID = 1L;

    public InvalidMultipleRecordException(final String message) {
        super(message);
    }

    public InvalidMultipleRecordException(final String message, final String record) {
        super(message, record);
    }
}
