package org.jfpa.manager;

import org.jfpa.annotation.Binary;
import org.jfpa.builder.BinaryRecordBuilder;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.exception.JfpaException;
import org.jfpa.interfaces.RecordHandler;
import org.jfpa.record.AbstractBinaryRecord;
import org.jfpa.utility.Utility;

import java.io.UnsupportedEncodingException;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 16/10/11
 */
public class BinaryRecordManager extends RecordManager {

    private final BinaryRecordBuilder<AbstractBinaryRecord> builder;

    public <T> BinaryRecordManager(final RecordHandler<String> handler, final Class<T> clazz) {
        loadBinary(clazz);
        AbstractBinaryRecord abstractBinaryRecord = loadBinary(clazz);
        RecordHandler<AbstractBinaryRecord> abstractBinaryRecordRecordHandler = new RecordHandler<AbstractBinaryRecord>() {
            public void handle(AbstractBinaryRecord record) throws InvalidRecordException {
                handler.handle(record.toString());
            }
        };
        builder = new BinaryRecordBuilder<AbstractBinaryRecord>(abstractBinaryRecord, abstractBinaryRecordRecordHandler);
    }

    public <T> BinaryRecordManager(final Class<T> clazz, final RecordHandler<T> handler) {
        if (super.loadClass(clazz) != Type.SINGLE) {
            throw new JfpaException(clazz, "Class must be @Positional or @Delimited to be used with BinaryRecordManager");
        }
        AbstractBinaryRecord abstractBinaryRecord = loadBinary(clazz);
        RecordHandler<AbstractBinaryRecord> abstractBinaryRecordRecordHandler = new RecordHandler<AbstractBinaryRecord>() {
            public void handle(AbstractBinaryRecord record) throws InvalidRecordException {
                handler.handle(read(record.toString(), clazz));
            }
        };
        builder = new BinaryRecordBuilder<AbstractBinaryRecord>(abstractBinaryRecord, abstractBinaryRecordRecordHandler);
    }

    private <T> AbstractBinaryRecord loadBinary(Class<T> clazz) {
        try {
            Binary binary = clazz.getAnnotation(Binary.class);
            if (binary == null) {
                throw new JfpaException(clazz, "Class doesn't contain @Binary annotation");
            }
            boolean hasPattern = binary.pattern().length > 0;
            boolean hasPatternString = !Utility.isEmpty(binary.patternString());
            boolean hasEncoding = !Utility.isEmpty(binary.encoding());
            if (!hasPattern && !hasPatternString) {
                throw new JfpaException(clazz, "Neither 'pattern' nor 'patternString' specified in @Binary");
            }
            if (hasPattern && hasPatternString) {
                throw new JfpaException(clazz, "Only one between 'pattern' and 'patternString' should be specified in @Binary");
            }
            final byte[] pattern = hasPattern ? binary.pattern()
                                   : hasEncoding ? binary.patternString().getBytes(binary.encoding())
                                   : binary.patternString().getBytes();
            return new AbstractBinaryRecord(binary.encoding()) {
                @Override
                public byte[] getPattern() {
                    return pattern;
                }
            };
        } catch (UnsupportedEncodingException e) {
            throw new JfpaException(clazz, e);
        }
    }

    public final void process(byte[] bytes) throws InvalidRecordException {
        builder.process(bytes);
    }

    public final void flush() throws InvalidRecordException {
        builder.flush();
    }
}
