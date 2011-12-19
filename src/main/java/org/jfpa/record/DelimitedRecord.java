package org.jfpa.record;

import org.jfpa.exception.InvalidRecordException;
import org.jfpa.type.RecordType;
import org.jfpa.utility.Utility;

import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 23/03/11
 */
public class DelimitedRecord extends AbstractRecord {

    private String[] columns;
    private String delimiter;
    private int[] lengths;

    public DelimitedRecord() { }

    public DelimitedRecord(final RecordType recordType) {
        setRecordType(recordType);
        this.columns = new String[recordType.getColumns()];
        this.delimiter = recordType.getDelimiter();
        this.lengths = recordType.getLengths();
    }

    public DelimitedRecord(final RecordType recordType, final String str) throws InvalidRecordException {
        buildRecord(recordType, str);
    }

    protected void buildRecord(RecordType recordType, String str) throws InvalidRecordException {
        checkString(str);
        setRecordType(recordType);
        delimiter = recordType.getDelimiter();
        lengths = recordType.getLengths();
        columns = str.split(Pattern.quote(delimiter), -1);
        if (columns.length < recordType.getColumns()) {
            throw new InvalidRecordException("Invalid number of getColumns: " + columns.length + " (expected: " + recordType.getColumns() + ")", str);
        }
        validate();
    }

    public int getColumns() {
        return columns.length;
    }

    @Override
    protected String getPos(int pos) throws InvalidRecordException {
        checkLength(pos, columns[pos]);
        return columns[pos];
    }

    @Override
    protected void setPos(int pos, String value) throws InvalidRecordException {
        checkLength(pos, value);
        this.columns[pos] = value;
    }

    private void checkLength(int pos, String value) throws InvalidRecordException {
        if (lengths != null && lengths[pos] > 0 && value != null && value.length() > lengths[pos])
            throw new InvalidRecordException("Value too large for pos " + pos + ": '" + value + "' has length " + value.length() + " (max: " + lengths[pos] + ")");
    }

    @Override
    public String toString() {
        return Utility.buildDelimitedString(delimiter, columns);
    }
}
