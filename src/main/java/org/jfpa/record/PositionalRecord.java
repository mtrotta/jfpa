package org.jfpa.record;

import org.jfpa.exception.InvalidRecordException;
import org.jfpa.type.RecordType;
import org.jfpa.utility.Utility;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 21/03/11
 */
public class PositionalRecord extends AbstractRecord {

    private int length;
    private int[] positions;
    private StringBuilder builder;

    public PositionalRecord() { }

    public PositionalRecord(final RecordType recordType) {
        setRecordType(recordType);
        buildRecord(recordType);
    }

    public PositionalRecord(final RecordType recordType, final String str) throws InvalidRecordException {
        buildRecord(recordType, str);
    }

    protected void buildRecord(RecordType recordType) {
        this.positions = recordType.getPositions();
        this.length = positions[positions.length - 1];
        this.builder = new StringBuilder(Utility.spaces(length));
    }

    protected final void buildRecord(RecordType recordType, String str) throws InvalidRecordException {
        checkString(str);
        setRecordType(recordType);
        buildRecord(recordType);
        if (str.length() < length) {
            throw new InvalidRecordException(String.format("Record has an invalid length: %d (expected %d)", str.length(), length), str);
        }
        builder.replace(0, length, str);
        validate();
    }

    protected final String getPos(int pos) throws InvalidRecordException {
        if (pos > positions.length - 2) {
            throw new InvalidRecordException("Invalid position: " + pos + " (max = " + (positions.length - 2) + ")");
        }
        return builder.substring(positions[pos], positions[pos + 1]);
    }

    protected final void setPos(int pos, String value) throws InvalidRecordException {
        int start = positions[pos];
        int end = positions[pos + 1];
        int maxLength = end - start;
        builder.replace(start, end, Utility.spaces(maxLength));
        if (value != null) {
            if (value.length() > maxLength) {
                throw new InvalidRecordException("Value too large for pos " + pos + ": '" + value + "' has length " + value.length() + " (max: " + maxLength + ")");
            }
            if (value.length() < maxLength) {
                end = start + value.length();
            }
            builder.replace(start, end, value);
        }
    }

    public final int getColumns() {
        return positions.length - 1;
    }

    @Override
    public String toString() {
        return builder.toString();
    }
}
