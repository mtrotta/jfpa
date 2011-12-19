package org.jfpa.cache;

import org.jfpa.type.RecordType;

import java.lang.reflect.Field;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 15/10/11
 */
public class CachedSubRecord {

    private Class<?> clazz;
    private Field field;
    private RecordType recordType;
    private boolean list;

    public CachedSubRecord(final Field field, final Class<?> clazz, final RecordType recordType, final boolean list) {
        this.field = field;
        this.clazz = clazz;
        this.recordType = recordType;
        this.list = list;
    }

    public final Field getField() {
        return field;
    }

    public final Class<?> getFieldClass() {
        return clazz;
    }

    public final RecordType getRecordType() {
        return recordType;
    }

    public final boolean isList() {
        return list;
    }
}
