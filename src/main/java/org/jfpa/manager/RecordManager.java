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

import org.jfpa.annotation.*;
import org.jfpa.cache.CachedColumn;
import org.jfpa.cache.CachedMultipleRecord;
import org.jfpa.cache.CachedRecord;
import org.jfpa.cache.CachedSubRecord;
import org.jfpa.exception.InvalidMultipleRecordException;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.exception.JfpaException;
import org.jfpa.interfaces.Converter;
import org.jfpa.interfaces.FlatRecord;
import org.jfpa.interfaces.MultipleRecordValidator;
import org.jfpa.interfaces.RecordValidator;
import org.jfpa.interfaces.TypeExtractor;
import org.jfpa.record.DelimitedRecord;
import org.jfpa.record.PositionalRecord;
import org.jfpa.type.RecordType;
import org.jfpa.utility.Formats;
import org.jfpa.utility.Utility;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecordManager {

    private Map<Class, Type> knownClasses = new HashMap<Class, Type>();
    private Map<Class, CachedRecord> singleClasses = new HashMap<Class, CachedRecord>();
    private Map<Class, CachedMultipleRecord> multipleClasses = new HashMap<Class, CachedMultipleRecord>();
    private Map<RecordType, Class> typeClasses = new HashMap<RecordType, Class>();
    private Map<Class, TypeExtractor> extractors = new HashMap<Class, TypeExtractor>();

    private String defaultDateFormat;
    private String[] defaultBooleanFormat;

    public static final String DEFAULT_DATE_FORMAT = Formats.DATE_FORMAT;
    public static final String[] DEFAULT_BOOLEAN_FORMAT = Formats.BOOLEAN_Y_N;

    public RecordManager() {
        this.defaultDateFormat = DEFAULT_DATE_FORMAT;
        this.defaultBooleanFormat = DEFAULT_BOOLEAN_FORMAT;
    }

    public RecordManager(final String defaultDateFormat, final String[] defaultBooleanFormat) {
        this.defaultDateFormat = defaultDateFormat;
        this.defaultBooleanFormat = defaultBooleanFormat;
    }

    public final <T> T read(String line, Class<T> clazz) throws InvalidRecordException {
        Type type = loadClass(clazz);
        T t = null;
        switch(type) {
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
        Type type = loadClass(clazz);
        String s = null;
        switch(type) {
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
        CachedMultipleRecord cachedMultipleRecord = multipleClasses.get(clazz);
        RecordType recordType = getRecordType(line, cachedMultipleRecord);
        CachedSubRecord cachedSubRecord = cachedMultipleRecord.getCachedSubRecord(recordType);
        CachedRecord cachedRecord = singleClasses.get(cachedSubRecord.getFieldClass());
        return getFlatRecord(line, cachedRecord.getSeparatorType(), cachedSubRecord.getRecordType());
    }

    private <T> T createRecord(FlatRecord record, Class<T> clazz) throws InvalidRecordException, IllegalAccessException, InstantiationException {
        T rootInstance = clazz.newInstance();
        CachedRecord cachedRecord = singleClasses.get(clazz);
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
                if (cachedColumn.isInvalidateOnError()) { throw e; }
            }
        }
        return rootInstance;
    }

    private <T> T readSingle(String line, Class<T> clazz) throws InvalidRecordException {
        try {
            CachedRecord cachedRecord = singleClasses.get(clazz);
            FlatRecord record = getFlatRecord(line, cachedRecord.getSeparatorType(), cachedRecord.getRecordType());
            T instance = createRecord(record, clazz);
            if (cachedRecord.isValidator()) {
                RecordValidator validator = RecordValidator.class.cast(instance);
                validator.validate();
            }
            return instance;
        } catch (IllegalAccessException e) {
            throw new JfpaException(clazz, e);
        } catch (InstantiationException e) {
            throw new JfpaException(clazz, e);
        }
    }

    private String writeSingle(Class clazz, Object instance) throws InvalidRecordException {
        CachedRecord cachedRecord = singleClasses.get(clazz);
        if (cachedRecord.isValidator()) {
            RecordValidator validator = RecordValidator.class.cast(instance);
            validator.validate();
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
                if (cachedColumn.isInvalidateOnError()) { throw e; }
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
            CachedMultipleRecord cachedMultipleRecord = multipleClasses.get(clazz);
            RecordType recordType = getRecordType(line, cachedMultipleRecord);
            CachedSubRecord cachedSubRecord = cachedMultipleRecord.getCachedSubRecord(recordType);
            Object object = readSingle(line, cachedSubRecord.getFieldClass());
            cachedSubRecord.getField().set(instance, cachedSubRecord.isList() ? Collections.singletonList(object) : object);
            if (cachedMultipleRecord.isValidator()) {
                MultipleRecordValidator validator = MultipleRecordValidator.class.cast(instance);
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
            CachedMultipleRecord cachedMultipleRecord = multipleClasses.get(clazz);
            T instance = clazz.newInstance();
            for (Map.Entry<RecordType, List<FlatRecord>> entry : records.entrySet()) {
                RecordType recordType = entry.getKey();
                Class<?> fieldClass = typeClasses.get(recordType);
                CachedSubRecord cachedSubRecord = cachedMultipleRecord.getCachedSubRecord(recordType);
                Field field = cachedSubRecord.getField();
                List<Object> list = new ArrayList<Object>();
                for (FlatRecord record : entry.getValue()) {
                    list.add(createRecord(record, fieldClass));
                }
                field.set(instance, cachedSubRecord.isList() ? list : list.get(0));
            }
            if (cachedMultipleRecord.isValidator()) {
                MultipleRecordValidator validator = MultipleRecordValidator.class.cast(instance);
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
            CachedMultipleRecord cachedMultipleRecord = multipleClasses.get(clazz);
            if (cachedMultipleRecord.isValidator()) {
                MultipleRecordValidator validator = MultipleRecordValidator.class.cast(instance);
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

    public final synchronized <T> Type loadClass(Class<T> clazz) {
        try {
            Type type = knownClasses.get(clazz);
            if (type == null) {
                Positional positional = clazz.getAnnotation(Positional.class);
                Delimited delimited = clazz.getAnnotation(Delimited.class);
                MultiplePositional multiplePositional = clazz.getAnnotation(MultiplePositional.class);
                MultipleDelimited multipleDelimited = clazz.getAnnotation(MultipleDelimited.class);
                boolean isPositional = positional != null;
                boolean isDelimited = delimited != null;
                boolean isMultiplePositional = multiplePositional != null;
                boolean isMultipleDelimited = multipleDelimited != null;
                boolean isSingle = isPositional || isDelimited;
                boolean isMultiple = isMultiplePositional || isMultipleDelimited;
                if (!isSingle && !isMultiple) {
                    throw new JfpaException(clazz, "Class doesn't contain any valid JFPA annotation");
                }
                if (isPositional && isDelimited) {
                    throw new JfpaException(clazz, "Class can't be @Positional and @Delimited at the same time");
                }
                if (isMultiplePositional && isMultipleDelimited) {
                    throw new JfpaException(clazz, "Class can't be @MultiplePositional and @MultipleDelimited at the same time");
                }
                if (isSingle && isMultiple) {
                    throw new JfpaException(clazz, "Class can't be @Positional|@Delimited and @MultiplePositional|@MultipleDelimited at the same time");
                }
                type = isSingle ? loadSingle(clazz, positional, delimited) : loadMultiple(clazz, multiplePositional, multipleDelimited);
                knownClasses.put(clazz, type);
            }
            return type;
        } catch (IllegalAccessException e) {
            throw new JfpaException(clazz, e);
        } catch (InstantiationException e) {
            throw new JfpaException(clazz, e);
        }
    }

    private <T> Type loadSingle(Class<T> clazz, Positional positional, Delimited delimited) {
        CachedRecord cachedRecord = loadRecord(clazz);
        Map<Field, CachedColumn> mapColumns = cachedRecord.getMapColumns();
        int pos = 0;
        if (positional != null) {
            List<Integer> lengths = new ArrayList<Integer>();
            int length = 0;
            for (CachedColumn cachedColumn : mapColumns.values()) {
                if (cachedColumn.getLength() <= 0) {
                    throw new JfpaException(clazz, String.format("Invalid 'length' for @Column '%s': %d (expected a positive integer)", cachedColumn.getName(), cachedColumn.getLength()));
                }
                if (cachedColumn.getOffset() > 0) {
                    pos++;
                    lengths.add(cachedColumn.getOffset());
                    length += cachedColumn.getOffset();
                }
                cachedColumn.setPosition(pos++);
                lengths.add(cachedColumn.getLength());
                length += cachedColumn.getLength();
            }
            if (positional.minLength() > 0 && length < positional.minLength()) {
                lengths.add(positional.minLength() - length);
            }
            cachedRecord.setPositional(Utility.convertArray(lengths));
        } else {
            if (Utility.isEmpty(delimited.delimiter())) {
                throw new JfpaException(clazz, "Invalid param 'delimiter': value is empty");
            }
            List<Integer> lengths = new ArrayList<Integer>();
            for (CachedColumn cachedColumn : mapColumns.values()) {
                if (cachedColumn.getOffset() > 0) {
                    for (int i = 0; i < cachedColumn.getOffset(); i++) {
                        pos++;
                        lengths.add(-1);
                    }
                }
                cachedColumn.setPosition(pos++);
                lengths.add(cachedColumn.getLength());
            }
            cachedRecord.setDelimited(delimited.delimiter(), delimited.minColumns() > 0 ? delimited.minColumns() : lengths.size(), Utility.convertArray(lengths));
        }
        List<Class<?>> interfaces = Arrays.asList(clazz.getInterfaces());
        cachedRecord.setValidator(interfaces.contains(RecordValidator.class));
        singleClasses.put(clazz, cachedRecord);
        return Type.SINGLE;
    }

    private <T> Type loadMultiple(Class<T> clazz, MultiplePositional multiplePositional, MultipleDelimited multipleDelimited) throws InstantiationException, IllegalAccessException {
        boolean positional = multiplePositional != null;
        CachedMultipleRecord cachedMultipleRecord = getCachedMultipleRecord(clazz, positional);
        List<Class<?>> interfaces = Arrays.asList(clazz.getInterfaces());
        boolean isTypeExtractor = interfaces.contains(TypeExtractor.class);
        if (positional) {
            int typePositionBegin = multiplePositional.typePositionBegin();
            int typePositionEnd = multiplePositional.typePositionEnd();
            boolean hasTypePositions = typePositionBegin >= 0 || typePositionEnd >= 0;
            boolean hasTypeExtractor = isTypeExtractor || !NullExtractor.class.equals(multiplePositional.typeExtractor());
            if (hasTypePositions && hasTypeExtractor) {
                throw new JfpaException(clazz, "Only one between 'typePosition' params and TypeExtractor interface should be specified");
            }
            if (!hasTypePositions && !hasTypeExtractor) {
                throw new JfpaException(clazz, "Unknown way to extract record type: no 'typePosition' or 'typeExtractor' specified, nor TypeExtractor implemented");
            }
            if (hasTypePositions) {
                if (typePositionBegin >= typePositionEnd || typePositionBegin < 0) {
                    throw new JfpaException(clazz, "Invalid positions: 'typePositionBegin' might be greater than 'typePositionEnd', they might have invalid size or one of them is missing");
                }
                cachedMultipleRecord.setPositional(typePositionBegin, typePositionEnd);
            } else {
                cachedMultipleRecord.setPositional(loadTypeExtractor(isTypeExtractor ? clazz : multiplePositional.typeExtractor()));
            }
        } else {
            String delimiter = multipleDelimited.delimiter();
            int typePosition = multipleDelimited.typePosition();
            boolean hasDelimiter = !Utility.isEmpty(delimiter);
            boolean hasTypePosition = typePosition >= 0;
            boolean hasTypeExtractor = isTypeExtractor || !NullExtractor.class.equals(multipleDelimited.typeExtractor());
            boolean hasExtractionRule = hasDelimiter || hasTypePosition;
            if (hasExtractionRule && hasTypeExtractor) {
                throw new JfpaException(clazz, "Only one between 'delimiter' or 'typePosition' params and TypeExtractor interface or 'typeExtractor' param should be specified ");
            }
            if (!(hasDelimiter || hasTypePosition) && !hasTypeExtractor) {
                throw new JfpaException(clazz, "Unable to extract record type: no 'typePosition' specified nor TypeExtractor implemented");
            }
            if (hasDelimiter && !hasTypePosition || !hasDelimiter && hasTypePosition) {
                throw new JfpaException(clazz, "One between 'delimiter' or 'typePosition' param is missing");
            }
            if (hasExtractionRule) {
                cachedMultipleRecord.setDelimited(delimiter, typePosition);
            } else {
                cachedMultipleRecord.setDelimited(loadTypeExtractor(isTypeExtractor ? clazz : multipleDelimited.typeExtractor()));
            }
        }
        cachedMultipleRecord.setValidator(interfaces.contains(MultipleRecordValidator.class));
        multipleClasses.put(clazz, cachedMultipleRecord);
        return Type.MULTIPLE;
    }

    private <T> CachedRecord loadRecord(Class<T> clazz) {
        Map<Field, CachedColumn> mapColumns = new LinkedHashMap<Field, CachedColumn>();
        Map<Field, Class> mapWrappedClasses = new LinkedHashMap<Field, Class>();
        loadColumns(clazz, mapColumns, mapWrappedClasses, null);
        return new CachedRecord(mapColumns, mapWrappedClasses);
    }
    
    private void loadColumns(Class<?> clazz, Map<Field, CachedColumn> mapColumns, Map<Field, Class> mapWrappedClasses, Field parentField) {
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Class columnClass = field.getType();
            Column column = field.getAnnotation(Column.class);
            WrappedColumns wrappedColumns = field.getAnnotation(WrappedColumns.class);
            if (column != null && wrappedColumns != null) {
                throw new JfpaException(clazz, "Only one between @Column and @WrappedColumns should be specified");
            }
            if (column != null) {
                ColumnType columnType = ColumnType.valueOf(columnClass);
                if (columnType == null) {
                    List<Class> interfaces = Arrays.asList(columnClass.getInterfaces());
                    if (!interfaces.contains(Converter.class)) {
                        throw new JfpaException(clazz, String.format("Invalid type %s for @Column '%s', please use one between %s or class %s must implement Converter interface",
                                columnClass.getName(), field.getName(), ColumnType.getValidTypes(), columnClass.getSimpleName()));
                    }
                    columnType = ColumnType.CUSTOM;
                }
                CachedColumn cachedColumn = new CachedColumn(field.getName(), columnType, column.offset(), column.invalidateOnError(), parentField);
                cachedColumn.setLength(column.length());
                boolean hasBooleanFormat = column.booleanFormat().length > 0;
                boolean hasDateFormat = !Utility.isEmpty(column.dateFormat());
                switch(columnType) {
                    case BOOLEAN:
                        if (hasDateFormat) {
                            throw new JfpaException(clazz, "Invalid 'dateFormat' parameter for boolean, 'booleanFormat' should be used instead");
                        }
                        if (hasBooleanFormat) {
                            String[] trueFalse = column.booleanFormat();
                            if (trueFalse.length != 2) {
                                throw new JfpaException(clazz, "Invalid booleanFormat '" + Arrays.toString(column.booleanFormat()) + "', should be a two valued String[] like {'true,false'}");
                            }
                            cachedColumn.setBooleanFormat(trueFalse);
                        } else {
                            cachedColumn.setBooleanFormat(defaultBooleanFormat);
                        }
                        break;
                    case DATE:
                        cachedColumn.setFormat(hasDateFormat ? column.dateFormat() : defaultDateFormat);
                        break;
                }
                mapColumns.put(field, cachedColumn);
            } else if (wrappedColumns != null) {
                if (parentField != null) {
                    throw new JfpaException(clazz, "Nested @WrappedColumns are not allowed");
                }
                mapWrappedClasses.put(field, columnClass);
                loadColumns(columnClass, mapColumns, mapWrappedClasses, field);
            }
        }
    }

    private CachedMultipleRecord getCachedMultipleRecord(Class clazz, boolean positional) {
        Map<String, RecordType> mapTypes = new LinkedHashMap<String, RecordType>();
        Map<RecordType, CachedSubRecord> mapFields = new LinkedHashMap<RecordType, CachedSubRecord>();
        List<CachedSubRecord> firsts = new ArrayList<CachedSubRecord>();
        for (Field field : clazz.getDeclaredFields()) {
            SubRecord subRecord = field.getAnnotation(SubRecord.class);
            if (subRecord != null) {
                boolean isList = false;
                Class<?> typeClass = field.getType();
                if (typeClass.equals(List.class)) {
                    if (!(field.getGenericType() instanceof ParameterizedType)) {
                        throw new JfpaException(clazz, String.format("SubRecord '%s' can't be a raw List, please use generics", field.getName()));
                    }
                    String className = ((ParameterizedType) field.getGenericType()).getActualTypeArguments()[0].toString();
                    if("?".equals(className)) {
                        throw new JfpaException(clazz, String.format("SubRecord '%s' can't be wildcard, please use a specific class", field.getName()));
                    }
                    try {
                        typeClass = Class.forName(className.substring(6));
                    } catch (ClassNotFoundException e) {
                        throw new JfpaException(clazz, e);
                    }
                    isList = true;
                }
                if (loadClass(typeClass) != Type.SINGLE) {
                    throw new JfpaException(clazz, "SubRecord class must be @Positional or @Delimited");
                }
                CachedRecord cachedRecord = singleClasses.get(typeClass);
                if (positional && cachedRecord.getSeparatorType() == SeparatorType.DELIMITED) {
                    throw new JfpaException(clazz, String.format("SubRecord '%s' can't be @Delimited in a @MultiplePositional record", field.getName()));
                } else if (!positional && cachedRecord.getSeparatorType() == SeparatorType.POSITIONAL) {
                    throw new JfpaException(clazz, String.format("SubRecord '%s' can't be @Positional in a @MultipleDelimited record", field.getName()));
                }
                field.setAccessible(true);
                RecordType recordType = new RecordType(subRecord.type(), singleClasses.get(typeClass).getRecordType());
                mapTypes.put(subRecord.type(), recordType);
                CachedSubRecord cachedSubRecord = new CachedSubRecord(field, typeClass, recordType, isList);
                mapFields.put(recordType, cachedSubRecord);
                if (firsts.isEmpty() || subRecord.first()) {
                    firsts.add(cachedSubRecord);
                }
                typeClasses.put(recordType, typeClass);
            }
        }
        return new CachedMultipleRecord(mapTypes, mapFields, firsts);
    }

    private TypeExtractor loadTypeExtractor(Class clazz) throws IllegalAccessException, InstantiationException {
        TypeExtractor extractor = extractors.get(clazz);
        if (extractor == null) {
            extractor = (TypeExtractor)clazz.newInstance();
            extractors.put(clazz, extractor);
        }
        return extractor;
    }

    protected final List<CachedSubRecord> getFirsts(final Class<?> clazz) {
        return multipleClasses.get(clazz).getFirsts();
    }

    public final String getDefaultDateFormat() {
        return defaultDateFormat;
    }

    public final String[] getDefaultBooleanFormat() {
        return defaultBooleanFormat;
    }
}
