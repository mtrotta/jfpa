package org.jfpa.type;

import org.jfpa.exception.JfpaException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 05/04/11
 */
public class RecordType {

    private String typeString;
    private int[] positions;
    private int[] lengths;
    private String delimiter;
    private Integer columns;

    public RecordType(final int[] lengths) {
        this.positions = convertLengthsToPositions(lengths);
        this.lengths = lengths;
    }

    public RecordType(final String typeString, final int[] lengths) {
        this(lengths);
        this.typeString = typeString;
    }

    public RecordType(final String delimiter, final int columns) {
        this.delimiter = delimiter;
        this.columns = columns;
    }

    public RecordType(final String delimiter, final int columns, final int[] lengths) {
        this(delimiter, columns);
        this.lengths = lengths;
    }

    public RecordType(final String typeString, final String delimiter, final int columns) {
        this(delimiter, columns);
        this.typeString = typeString;
    }

    public RecordType(final String typeString, final String delimiter, final int columns, final int[] lengths) {
        this(delimiter, columns, lengths);
        this.typeString = typeString;
    }

    public RecordType(final String type, final RecordType recordType) {
        this.typeString = type;
        this.positions = recordType.getPositions();
        this.lengths = recordType.getLengths();
        this.delimiter = recordType.getDelimiter();
        this.columns = recordType.getColumns();
    }

    public final String getTypeString() {
        return typeString;
    }

    public int[] getPositions() {
        return positions;
    }

    public int[] getLengths() {
        return lengths;
    }

    public Integer getColumns() {
        return columns;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public static Map<String, RecordType> buildMap(RecordType... types) {
        Map<String, RecordType> map = new HashMap<String, RecordType>();
        for (RecordType type : types) {
            map.put(type.getTypeString(), type);
        }
        return map;
    }

    public static int[] convertLengthsToPositions(int... columns) {
        int[] positions = new int[columns.length + 1];
        positions[0] = 0;
        for (int i = 0; i < columns.length; i++) {
            if (columns[i] <= 0) {
                throw new JfpaException(String.format("Invalid length at position %d: %d (expected a positive integer)", i, columns[i]));
            }
            positions[i + 1] = positions[i] + columns[i];
        }
        return positions;
    }
}
