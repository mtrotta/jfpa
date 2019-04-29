/*
 * Copyright (c) 2012 Matteo Trotta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jfpa.manager;

import org.jfpa.cache.CachedColumn;
import org.jfpa.cache.CachedMultipleRecord;
import org.jfpa.cache.CachedRecord;
import org.jfpa.cache.CachedSubRecord;
import org.jfpa.exception.InvalidMultipleRecordException;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.exception.JfpaException;
import org.jfpa.interfaces.*;
import org.jfpa.record.DelimitedRecord;
import org.jfpa.record.PositionalRecord;
import org.jfpa.type.RecordType;
import org.jfpa.utility.Formats;
import org.jfpa.utility.Utility;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.*;

public class RecordManager {

    public static final String DEFAULT_DATE_FORMAT = Formats.DATE_FORMAT;
    public static final String[] DEFAULT_BOOLEAN_FORMAT = Formats.BOOLEAN_Y_N;

    protected final RecordClassLoader recordClassLoader;

    public RecordManager() {
        this.recordClassLoader = new RecordClassLoader(DEFAULT_DATE_FORMAT, DEFAULT_BOOLEAN_FORMAT);
    }

    public RecordManager(final String defaultDateFormat, final String[] defaultBooleanFormat) {
        this.recordClassLoader = new RecordClassLoader(defaultDateFormat, defaultBooleanFormat);
    }

    public final <T> String writeHeader(Class<T> clazz) throws InvalidRecordException {
        Type type = recordClassLoader.loadClass(clazz);
        String header = null;
        switch (type) {
            case SINGLE:
                header = writeSingle(clazz, createHeader(clazz));
                break;
            case MULTIPLE:
                throw new JfpaException("header for Multiple record type is not supported");
        }
        return header;
    }

    private <T> T createHeader(Class<T> clazz) {
        try {
            T instance = clazz.newInstance();
            CachedRecord cachedRecord = recordClassLoader.getCachedRecord(clazz);
            for (Map.Entry<Field, CachedColumn> entry : cachedRecord.getMapColumns().entrySet()) {
                Field field = entry.getKey();
                CachedColumn cachedColumn = entry.getValue();
                field.set(instance, cachedColumn.getName());
            }
            return instance;
        } catch (IllegalAccessException e) {
            throw new JfpaException(clazz, e);
        } catch (InstantiationException e) {
            throw new JfpaException(clazz, e);
        }
    }

    public <T> void mapFromHeader(Class<T> clazz, String header) throws InvalidRecordException {
        Type type = recordClassLoader.loadClass(clazz);
        switch (type) {
            case SINGLE:
                CachedRecord cachedRecord = recordClassLoader.getCachedRecord(clazz);
                Map<String, CachedColumn> mapNames = cachedRecord.getMapNames();
                Map<Field, CachedColumn> mapColumns = cachedRecord.getMapColumns();
                if (mapNames.size() < mapColumns.size()) {
                    mapColumns.values().removeAll(mapNames.values());
                    throw new JfpaException("unable to map from header, record contains unnamed columns: " + mapColumns.values());
                }
                FlatRecord record = getFlatRecord(header, cachedRecord.getSeparatorType(), cachedRecord.getRecordType());
                Set<String> found = new LinkedHashSet<String>();
                for (int i = 0; i < record.getColumns(); i++) {
                    String name = record.getString(i);
                    CachedColumn cachedColumn = mapNames.get(name);
                    if (cachedColumn != null) {
                        cachedColumn.setPosition(i);
                        found.add(name);
                    }
                }
                if (found.size() < mapNames.size()) {
                    Set<String> missing = new LinkedHashSet<String>(mapNames.keySet());
                    missing.removeAll(found);
                    throw new InvalidRecordException("not all columns have been mapped from header: " + missing);
                }
                break;
            case MULTIPLE:
                throw new JfpaException("header mapping for Multiple record type is not supported");
        }
    }

    public final <T> T read(String line, Class<T> clazz) throws InvalidRecordException {
        Type type = recordClassLoader.loadClass(clazz);
        T t = null;
        switch (type) {
            case SINGLE:
                t = readSingle(line, clazz);
                break;
            case MULTIPLE:
                t = readMultiple(line, clazz);
        }
        return t;
    }

    public final String write(Object instance) throws InvalidRecordException {
        Class<?> clazz = instance.getClass();
        Type type = recordClassLoader.loadClass(clazz);
        String s = null;
        switch (type) {
            case SINGLE:
                s = writeSingle(clazz, instance);
                break;
            case MULTIPLE:
                s = writeMultiple(clazz, instance);
        }
        return s;
    }

    protected final FlatRecord getFlatRecord(String line, SeparatorType separatorType, RecordType recordType) throws InvalidRecordException {
        FlatRecord record = null;
        switch (separatorType) {
            case POSITIONAL:
                record = new PositionalRecord(recordType, line);
                break;
            case DELIMITED:
                record = new DelimitedRecord(recordType, line);
        }
        return record;
    }

    protected final FlatRecord getVariableFlatRecord(String line, Class<?> clazz) throws InvalidRecordException {
        CachedMultipleRecord cachedMultipleRecord = recordClassLoader.getCachedMultipleRecord(clazz);
        RecordType recordType = getRecordType(line, cachedMultipleRecord);
        CachedSubRecord cachedSubRecord = cachedMultipleRecord.getCachedSubRecord(recordType);
        CachedRecord cachedRecord = recordClassLoader.getCachedRecord(cachedSubRecord.getFieldClass());
        return getFlatRecord(line, cachedRecord.getSeparatorType(), cachedSubRecord.getRecordType());
    }

    private <T> T createRecord(FlatRecord record, Class<T> clazz) throws InvalidRecordException, IllegalAccessException, InstantiationException {
        T rootInstance = clazz.newInstance();
        CachedRecord cachedRecord = recordClassLoader.getCachedRecord(clazz);
        for (Map.Entry<Field, Class> entry : cachedRecord.getMapWrappedClasses().entrySet()) {
            Field field = entry.getKey();
            Object wrappedInstance = entry.getValue().newInstance();
            field.set(rootInstance, wrappedInstance);
        }
        for (Map.Entry<Field, CachedColumn> entry : cachedRecord.getMapColumns().entrySet()) {
            Field field = entry.getKey();
            CachedColumn cachedColumn = entry.getValue();
            Object instance = cachedColumn.isWrapped() ? cachedColumn.getParentField().get(rootInstance) : rootInstance;
            try {
                switch (cachedColumn.getColumnType()) {
                    case STRING:
                        field.set(instance, record.getString(cachedColumn.getPosition()));
                        break;
                    case DATE:
                        field.set(instance, record.getDate(cachedColumn.getPosition(), cachedColumn.getFormat()));
                        break;
                    case INTEGER:
                        field.set(instance, record.getInteger(cachedColumn.getPosition()));
                        break;
                    case LONG:
                        field.set(instance, record.getLong(cachedColumn.getPosition()));
                        break;
                    case DOUBLE:
                        field.set(instance, record.getDouble(cachedColumn.getPosition()));
                        break;
                    case BIG_DECIMAL:
                        field.set(instance, record.getBigDecimal(cachedColumn.getPosition()));
                        break;
                    case BOOLEAN:
                        field.set(instance, record.getBoolean(cachedColumn.getPosition(), cachedColumn.getBooleanFormat()));
                        break;
                    case CUSTOM:
                        Object typeInstance = field.getType().newInstance();
                        ((Converter) typeInstance).write(record.getString(cachedColumn.getPosition()));
                        field.set(instance, typeInstance);
                }
            } catch (InvalidRecordException e) {
                if (cachedColumn.isInvalidateOnError()) {
                    throw e;
                }
            }
        }
        return rootInstance;
    }

    private <T> T readSingle(String line, Class<T> clazz) throws InvalidRecordException {
        try {
            CachedRecord cachedRecord = recordClassLoader.getCachedRecord(clazz);
            FlatRecord record = getFlatRecord(line, cachedRecord.getSeparatorType(), cachedRecord.getRecordType());
            T instance = createRecord(record, clazz);
            if (cachedRecord.isValidator()) {
                RecordValidator validator = (RecordValidator) instance;
                validator.validate();
            }
            for (Method method : cachedRecord.getPostReadMethods()) {
                method.invoke(instance);
            }
            return instance;
        } catch (InvocationTargetException e) {
            throw new JfpaException(clazz, e);
        } catch (IllegalAccessException e) {
            throw new JfpaException(clazz, e);
        } catch (InstantiationException e) {
            throw new JfpaException(clazz, e);
        }
    }

    private String writeSingle(Class clazz, Object instance) throws InvalidRecordException {
        CachedRecord cachedRecord = recordClassLoader.getCachedRecord(clazz);
        if (cachedRecord.isValidator()) {
            RecordValidator validator = (RecordValidator) instance;
            validator.validate();
        }
        try {
            for (Method method : cachedRecord.getPreWriteMethods()) {
                method.invoke(instance);
            }
        } catch (IllegalAccessException e) {
            throw new JfpaException(clazz, e);
        } catch (InvocationTargetException e) {
            throw new JfpaException(clazz, e);
        }
        FlatRecord record = cachedRecord.getSeparatorType() == SeparatorType.POSITIONAL
                ? new PositionalRecord(cachedRecord.getRecordType())
                : new DelimitedRecord(cachedRecord.getRecordType());
        setFields(record, instance, cachedRecord.getMapColumns());
        return record.toString();
    }

    private void setFields(FlatRecord record, Object rootInstance, Map<Field, CachedColumn> mapColumns) throws InvalidRecordException {
        for (Map.Entry<Field, CachedColumn> entry : mapColumns.entrySet()) {
            Field field = entry.getKey();
            CachedColumn cachedColumn = entry.getValue();
            try {
                Object instance = cachedColumn.isWrapped() ? cachedColumn.getParentField().get(rootInstance) : rootInstance;
                switch (cachedColumn.getColumnType()) {
                    case STRING:
                        record.setString(cachedColumn.getPosition(), (String) field.get(instance));
                        break;
                    case DATE:
                        record.setDate(cachedColumn.getPosition(), (Date) field.get(instance), cachedColumn.getFormat());
                        break;
                    case INTEGER:
                        record.setInteger(cachedColumn.getPosition(), (Integer) field.get(instance));
                        break;
                    case LONG:
                        record.setLong(cachedColumn.getPosition(), (Long) field.get(instance));
                        break;
                    case DOUBLE:
                        record.setDouble(cachedColumn.getPosition(), (Double) field.get(instance));
                        break;
                    case BIG_DECIMAL:
                        record.setBigDecimal(cachedColumn.getPosition(), (BigDecimal) field.get(instance));
                        break;
                    case BOOLEAN:
                        record.setBoolean(cachedColumn.getPosition(), (Boolean) field.get(instance), cachedColumn.getBooleanFormat());
                        break;
                    case CUSTOM:
                        Object typeInstance = field.get(instance);
                        record.setString(cachedColumn.getPosition(), ((Converter) typeInstance).read());
                }
            } catch (InvalidRecordException e) {
                if (cachedColumn.isInvalidateOnError()) {
                    throw e;
                }
            } catch (IllegalAccessException e) {
                throw new JfpaException(e);
            }
        }
    }

    protected final RecordType getRecordType(String line, CachedMultipleRecord cachedMultipleRecord) throws InvalidRecordException {
        String typeString = null;
        TypeExtractor typeExtractor = cachedMultipleRecord.getTypeExtractor();
        if (typeExtractor != null) {
            typeString = typeExtractor.extractType(line);
        } else {
            switch (cachedMultipleRecord.getSeparatorType()) {
                case POSITIONAL:
                    int begin = cachedMultipleRecord.getTypePositionBegin();
                    int end = cachedMultipleRecord.getTypePositionEnd();
                    if (end > line.length()) {
                        throw new InvalidRecordException(String.format("Unable to extract type, position %d exceeds line length (%d)", end, line.length()), line);
                    }
                    typeString = Utility.substring(line, begin, end, true);
                    break;
                case DELIMITED:
                    String[] columns = line.split(cachedMultipleRecord.getDelimiter());
                    int pos = cachedMultipleRecord.getTypePosition();
                    if (pos >= columns.length) {
                        throw new InvalidRecordException(String.format("Unable to extract type, position %d exceeds getColumns number (%d)", cachedMultipleRecord.getTypePosition(), columns.length), line);
                    }
                    typeString = columns[cachedMultipleRecord.getTypePosition()];
            }
        }
        RecordType recordType = cachedMultipleRecord.getType(typeString);
        if (recordType == null) {
            throw new InvalidRecordException(String.format("Invalid type '%s'", typeString), line);
        }
        return recordType;
    }

    private <T> T readMultiple(String line, Class<T> clazz) throws InvalidRecordException {
        try {
            T instance = clazz.newInstance();
            CachedMultipleRecord cachedMultipleRecord = recordClassLoader.getCachedMultipleRecord(clazz);
            RecordType recordType = getRecordType(line, cachedMultipleRecord);
            CachedSubRecord cachedSubRecord = cachedMultipleRecord.getCachedSubRecord(recordType);
            Object object = readSingle(line, cachedSubRecord.getFieldClass());
            cachedSubRecord.getField().set(instance, cachedSubRecord.isList() ? Collections.singletonList(object) : object);
            if (cachedMultipleRecord.isValidator()) {
                MultipleRecordValidator validator = (MultipleRecordValidator) instance;
                validator.validate();
            }
            return instance;
        } catch (IllegalAccessException e) {
            throw new JfpaException(clazz, e);
        } catch (InstantiationException e) {
            throw new JfpaException(clazz, e);
        }
    }

    protected final <T> T readMultiple(Map<RecordType, List<FlatRecord>> records, Class<T> clazz) throws InvalidRecordException {
        try {
            CachedMultipleRecord cachedMultipleRecord = recordClassLoader.getCachedMultipleRecord(clazz);
            T instance = clazz.newInstance();
            for (Map.Entry<RecordType, List<FlatRecord>> entry : records.entrySet()) {
                RecordType recordType = entry.getKey();
                Class<?> fieldClass = recordClassLoader.getTypeClass(recordType);
                CachedSubRecord cachedSubRecord = cachedMultipleRecord.getCachedSubRecord(recordType);
                Field field = cachedSubRecord.getField();
                List<Object> list = new ArrayList<Object>();
                for (FlatRecord record : entry.getValue()) {
                    list.add(createRecord(record, fieldClass));
                }
                field.set(instance, cachedSubRecord.isList() ? list : list.get(0));
            }
            if (cachedMultipleRecord.isValidator()) {
                MultipleRecordValidator validator = (MultipleRecordValidator) instance;
                validator.validate();
            }
            return instance;
        } catch (IllegalAccessException e) {
            throw new JfpaException(clazz, e);
        } catch (InstantiationException e) {
            throw new JfpaException(clazz, e);
        }
    }

    public final String writeMultiple(Class<?> clazz, Object instance) throws InvalidRecordException {
        try {
            CachedMultipleRecord cachedMultipleRecord = recordClassLoader.getCachedMultipleRecord(clazz);
            if (cachedMultipleRecord.isValidator()) {
                MultipleRecordValidator validator = (MultipleRecordValidator) instance;
                validator.validate();
            }
            List<String> records = new ArrayList<String>();
            for (CachedSubRecord cachedSubRecord : cachedMultipleRecord.getAllFields()) {
                Field field = cachedSubRecord.getField();
                Object value = field.get(instance);
                if (value != null) {
                    if (cachedSubRecord.isList()) {
                        List list = (List) value;
                        for (Object object : list) {
                            records.add(writeSingle(cachedSubRecord.getFieldClass(), object));
                        }
                    } else {
                        records.add(writeSingle(cachedSubRecord.getFieldClass(), value));
                    }
                }
            }
            if (records.isEmpty()) {
                throw new InvalidMultipleRecordException("No sub records found for record " + clazz.getName());
            }
            return Utility.buildNewLineString(records);
        } catch (IllegalAccessException e) {
            throw new JfpaException(clazz, e);
        }
    }

}
