package org.jfpa.type;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 05/04/11
 */
public class MultipleRecordType {

    private String typeString;

    public MultipleRecordType() { }

    public MultipleRecordType(final String typeString) {
        this.typeString = typeString;
    }

    public final String getTypeString() {
        return typeString;
    }

    public static Map<String, MultipleRecordType> buildMap(MultipleRecordType... types) {
        Map<String, MultipleRecordType> map = new HashMap<String, MultipleRecordType>();
        for (MultipleRecordType type : types) {
            map.put(type.getTypeString(), type);
        }
        return map;
    }
}
