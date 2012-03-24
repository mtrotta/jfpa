package org.jfpa.record;

import org.jfpa.exception.InvalidMultipleRecordException;
import org.jfpa.interfaces.MultipleTypeExtractor;
import org.jfpa.type.MultipleRecordType;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 21/03/11
 */
public abstract class AbstractMultipleVariableRecord extends AbstractMultipleRecord implements MultipleTypeExtractor {

    private Map<String, MultipleRecordType> mapRecordType;

    protected AbstractMultipleVariableRecord(Map<String, MultipleRecordType> mapRecordType) {
        this.mapRecordType = mapRecordType;
    }

    @Override
    public MultipleRecordType getType() throws InvalidMultipleRecordException {
        String typeString = extractType();
        MultipleRecordType type = mapRecordType.get(typeString);
        if (type == null) {
            throw new InvalidMultipleRecordException(String.format("Unexpected record type: '%s'", typeString), toString());
        }
        return type;
    }

}
