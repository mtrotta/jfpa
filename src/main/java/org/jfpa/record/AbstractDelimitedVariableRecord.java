package org.jfpa.record;

import org.jfpa.exception.InvalidRecordException;
import org.jfpa.interfaces.TypeExtractor;
import org.jfpa.type.RecordType;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 25/03/11
 */
public abstract class AbstractDelimitedVariableRecord extends DelimitedRecord implements TypeExtractor {

    protected AbstractDelimitedVariableRecord(RecordType recordType, String str) throws InvalidRecordException {
        buildRecord(recordType, str);
    }

    protected AbstractDelimitedVariableRecord(Map<String, RecordType> mapRecordType, String str) throws InvalidRecordException {
        String typeString = extractType(str);
        RecordType recordType = mapRecordType.get(typeString);
        if (recordType == null) {
            throw new InvalidRecordException(String.format("Unexpected record type: '%s'", typeString), str);
        }
        buildRecord(recordType, str);
    }
}
