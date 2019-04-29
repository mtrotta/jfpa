package org.jfpa.cache;

import org.jfpa.manager.ColumnType;

import java.lang.reflect.Field;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 02/10/11
 */
public class CachedColumn {
    private final String fieldName;
    private final String name;
    private final ColumnType columnType;
    private int length;
    private final int offset;
    private int position;
    private String format;
    private String[] booleanFormat;
    private final boolean invalidateOnError;
    private final Field parentField;

    public CachedColumn(final String fieldName, final String name, final ColumnType columnType, final int offset, final boolean invalidateOnError, final Field parentField) {
        this.fieldName = fieldName;
        this.name = name;
        this.offset = offset;
        this.columnType = columnType;
        this.invalidateOnError = invalidateOnError;
        this.parentField = parentField;
    }

    public final String getFieldName() {
        return fieldName;
    }

    public String getName() {
        return name;
    }

    public final ColumnType getColumnType() {
        return columnType;
    }

    public final int getPosition() {
        return position;
    }

    public final void setPosition(final int position) {
        this.position = position;
    }

    public final int getLength() {
        return length;
    }

    public final int getOffset() {
        return offset;
    }

    public final void setLength(final int length) {
        this.length = length;
    }

    public final String getFormat() {
        return format;
    }

    public final void setFormat(final String format) {
        this.format = format;
    }

    public final String[] getBooleanFormat() {
        return booleanFormat;
    }

    public final void setBooleanFormat(final String[] booleanFormat) {
        this.booleanFormat = booleanFormat;
    }

    public final boolean isInvalidateOnError() {
        return invalidateOnError;
    }
    
    public final Field getParentField() {
        return this.parentField;
    }

    public final boolean isWrapped() {
        return parentField != null;
    }

    @Override
    public String toString() {
        return fieldName;
    }
}
