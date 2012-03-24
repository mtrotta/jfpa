package org.jfpa.exception;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 22/03/11
 */
public class InvalidRecordException extends Exception {

    private static final long serialVersionUID = 1L;

    private String record;

    public InvalidRecordException(final String message) {
        super(message);
    }

    public InvalidRecordException(final String message, final String record) {
        super(message);
        this.record = record;
    }

    public final String getRecord() {
        return record;
    }
}
