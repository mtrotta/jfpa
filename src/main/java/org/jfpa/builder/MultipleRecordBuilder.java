package org.jfpa.builder;

import org.jfpa.exception.InvalidMultipleRecordException;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.interfaces.FlatRecord;
import org.jfpa.interfaces.MultipleFlatRecord;
import org.jfpa.interfaces.RecordHandler;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 24/03/11
 */
public class MultipleRecordBuilder<T extends MultipleFlatRecord> {

    private final T record;
    private final RecordHandler<T> handler;

    public MultipleRecordBuilder(final T record, final RecordHandler<T> handler) {
        this.record = record;
        this.handler = handler;
    }

    public final void add(final FlatRecord flatRecord) throws InvalidRecordException {
        if (record.isFirst(flatRecord)) {
            try {
                flush();
            } finally {
                record.addRecord(flatRecord);
            }
        } else if (record.isEmpty()) {
            throw new InvalidMultipleRecordException("Out of sync: missing header for record '" + flatRecord + "'", record.toString());
        } else {
            record.addRecord(flatRecord);
        }
    }

    public final void flush() throws InvalidRecordException {
        try {
            if (!record.isEmpty()) {
                record.validate();
                handler.handle(record);
            }
        } finally {
            record.clear();
        }
    }
}
