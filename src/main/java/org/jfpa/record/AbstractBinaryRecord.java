package org.jfpa.record;

import org.jfpa.exception.InvalidRecordException;
import org.jfpa.exception.JfpaException;
import org.jfpa.interfaces.Record;
import org.jfpa.interfaces.RecordValidator;
import org.jfpa.utility.Utility;

import java.io.UnsupportedEncodingException;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 13/04/11
 */
public abstract class AbstractBinaryRecord implements Record, RecordValidator {

    protected byte[] bytes;
    protected String encoding;

    protected AbstractBinaryRecord() { }

    protected AbstractBinaryRecord(final byte[] bytes) throws InvalidRecordException {
        buildRecord(bytes);
    }

    protected AbstractBinaryRecord(final byte[] bytes, final String encoding) throws InvalidRecordException {
        buildRecord(bytes);
        this.encoding = encoding;
    }

    protected AbstractBinaryRecord(final String encoding) {
        this.encoding = encoding;
    }

    public final void buildRecord(byte[] bytes) throws InvalidRecordException {
        this.bytes = bytes;
        validate();
    }

    public void validate() throws InvalidRecordException {
        if (!isComplete()) {
            invalidateRecord("record is not complete");
        }
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    protected void invalidateRecord(String message) throws InvalidRecordException {
        throw new InvalidRecordException("Record didn't pass validation: " + message, toString());
    }

    public boolean isComplete() {
        return bytes != null;
    }

    @Override
    public String toString() {
        try {
            return Utility.isEmpty(encoding) ? new String(bytes) : new String(bytes, encoding);
        } catch (UnsupportedEncodingException e) {
            throw new JfpaException(e);
        }
    }

    public abstract byte[] getPattern();
}
