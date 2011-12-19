package org.jfpa.manager;

import org.jfpa.builder.MultipleRecordBuilder;
import org.jfpa.cache.CachedSubRecord;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.exception.JfpaException;
import org.jfpa.interfaces.FlatRecord;
import org.jfpa.interfaces.MultipleFlatRecord;
import org.jfpa.interfaces.RecordHandler;
import org.jfpa.record.AbstractMultipleRecord;
import org.jfpa.type.RecordType;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 11/10/11
 */
public class MultipleRecordManager extends RecordManager {

    private final Class<?> clazz;
    private MultipleRecordBuilder<MultipleFlatRecord> builder;

    public <T> MultipleRecordManager(final Class<T> clazz, final RecordHandler<T> handler) {
        this.clazz = clazz;
        if (super.loadClass(clazz) != Type.MULTIPLE) {
            throw new JfpaException(clazz, "class must be @MultiplePositional or @MultipleDelimited to be used with MultipleRecordManager");
        }
        List<CachedSubRecord> firsts = getFirsts(clazz);
        final RecordType[] firstRecordTypes = new RecordType[firsts.size()];
        for (int i = 0; i < firsts.size(); i++) {
            CachedSubRecord first = firsts.get(i);
            if (first.isList()) {
                throw new JfpaException(clazz, "First @SubRecord can't be a List");
            }
            firstRecordTypes[i] = first.getRecordType();
        }
        MultipleFlatRecord multipleFlatRecord = new AbstractMultipleRecord() {
            public boolean isFirst(FlatRecord record) {
                return record.isType(firstRecordTypes);
            }
        };
        RecordHandler<MultipleFlatRecord> multipleFlatRecordRecordHandler = new RecordHandler<MultipleFlatRecord>() {
            public void handle(MultipleFlatRecord record) throws InvalidRecordException {
                handler.handle(readMultiple(record.getRecords(), clazz));
            }
        };
        builder = new MultipleRecordBuilder<MultipleFlatRecord>(multipleFlatRecord, multipleFlatRecordRecordHandler);
    }

    public final void read(final String line) throws InvalidRecordException {
        builder.add(getVariableFlatRecord(line, clazz));
    }

    public final void flush() throws InvalidRecordException {
        builder.flush();
    }
}
