package org.jfpa.record;

import org.jfpa.exception.InvalidRecordException;
import org.jfpa.interfaces.TypeExtractor;
import org.jfpa.type.RecordType;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 22/03/11
 */
public abstract class AbstractPositionalVariableRecord extends PositionalRecord implements TypeExtractor {

    protected AbstractPositionalVariableRecord(RecordType recordType, String str) throws InvalidRecordException {
        buildRecord(recordType, str);
    }

    protected AbstractPositionalVariableRecord(Map<String, RecordType> mapRecordType, String str) throws InvalidRecordException {
        String typeString = extractType(str);
        RecordType recordType = mapRecordType.get(typeString);
        if (recordType == null) {
            throw new InvalidRecordException(String.format("Unexpected record type: '%s'", typeString), str);
        }
        buildRecord(recordType, str);
    }
}
