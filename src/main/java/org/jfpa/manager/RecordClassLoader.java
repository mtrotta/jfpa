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
import org.jfpa.exception.JfpaException;
import org.jfpa.interfaces.Converter;
import org.jfpa.interfaces.MultipleRecordValidator;
import org.jfpa.interfaces.RecordValidator;
import org.jfpa.interfaces.TypeExtractor;
import org.jfpa.type.RecordType;
import org.jfpa.utility.Utility;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.*;

public class RecordClassLoader {

    private Map<Class, Type> knownClasses = new HashMap<Class, Type>();
    private Map<Class, CachedRecord> singleClasses = new HashMap<Class, CachedRecord>();
    private Map<Class, CachedMultipleRecord> multipleClasses = new HashMap<Class, CachedMultipleRecord>();
    private Map<RecordType, Class> typeClasses = new HashMap<RecordType, Class>();
    private Map<Class, TypeExtractor> extractors = new HashMap<Class, TypeExtractor>();

    private String defaultDateFormat;
    private String[] defaultBooleanFormat;

    public RecordClassLoader(String defaultDateFormat, String[] defaultBooleanFormat) {
        this.defaultDateFormat = defaultDateFormat;
        this.defaultBooleanFormat = defaultBooleanFormat;
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
                    throw new JfpaException(clazz, String.format("Invalid 'length' for @TextColumn '%s': %d (expected a positive integer)", cachedColumn.getFieldName(), cachedColumn.getLength()));
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
            cachedRecord.setDelimited(delimited.delimiter(),
                    delimited.minColumns() > 0 ? delimited.minColumns() : lengths.size(),
                    delimited.stringEnclose().isEmpty() ? null : delimited.stringEnclose(),
                    Utility.convertArray(lengths));
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
        Map<String, CachedColumn> mapNames = new LinkedHashMap<String, CachedColumn>();
        List<Method> postReadMethods = new ArrayList<Method>();
        List<Method> preWriteMethods = new ArrayList<Method>();
        loadColumns(clazz, mapColumns, mapWrappedClasses, mapNames, null);
        loadMethods(clazz, postReadMethods, preWriteMethods);
        return new CachedRecord(mapColumns, mapWrappedClasses, mapNames, postReadMethods, preWriteMethods);
    }

    private void loadColumns(Class<?> clazz, Map<Field, CachedColumn> mapColumns, Map<Field, Class> mapWrappedClasses,
                             Map<String, CachedColumn> mapNames, Field parentField) {
        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            Class columnClass = field.getType();
            TextColumn textColumn = field.getAnnotation(TextColumn.class);
            WrappedColumns wrappedColumns = field.getAnnotation(WrappedColumns.class);
            if (textColumn != null && wrappedColumns != null) {
                throw new JfpaException(clazz, "Only one between @TextColumn and @WrappedColumns should be specified");
            }
            if (textColumn != null) {
                ColumnType columnType = ColumnType.valueOf(columnClass);
                if (columnType == null) {
                    List<Class> interfaces = Arrays.asList(columnClass.getInterfaces());
                    if (!interfaces.contains(Converter.class)) {
                        throw new JfpaException(clazz, String.format("Invalid type %s for @TextColumn '%s', please use one between %s or class %s must implement Converter interface",
                                columnClass.getName(), field.getName(), ColumnType.getValidTypes(), columnClass.getSimpleName()));
                    }
                    columnType = ColumnType.CUSTOM;
                }
                CachedColumn cachedColumn = new CachedColumn(field.getName(), textColumn.name(), columnType, textColumn.offset(), textColumn.invalidateOnError(), parentField);
                cachedColumn.setLength(textColumn.length());
                boolean hasBooleanFormat = textColumn.booleanFormat().length > 0;
                boolean hasDateFormat = !Utility.isEmpty(textColumn.dateFormat());
                switch (columnType) {
                    case BOOLEAN:
                        if (hasDateFormat) {
                            throw new JfpaException(clazz, "Invalid 'dateFormat' parameter for boolean, 'booleanFormat' should be used instead");
                        }
                        if (hasBooleanFormat) {
                            String[] trueFalse = textColumn.booleanFormat();
                            if (trueFalse.length != 2) {
                                throw new JfpaException(clazz, "Invalid booleanFormat '" + Arrays.toString(textColumn.booleanFormat()) + "', should be a two valued String[] like {'true,false'}");
                            }
                            cachedColumn.setBooleanFormat(trueFalse);
                        } else {
                            cachedColumn.setBooleanFormat(defaultBooleanFormat);
                        }
                        break;
                    case DATE:
                        cachedColumn.setFormat(hasDateFormat ? textColumn.dateFormat() : defaultDateFormat);
                        break;
                }
                mapColumns.put(field, cachedColumn);
                if (!textColumn.name().isEmpty()) {
                    mapNames.put(textColumn.name(), cachedColumn);
                }
            } else if (wrappedColumns != null) {
                if (parentField != null) {
                    throw new JfpaException(clazz, "Nested @WrappedColumns are not allowed");
                }
                mapWrappedClasses.put(field, columnClass);
                loadColumns(columnClass, mapColumns, mapWrappedClasses, mapNames, field);
            }
        }
    }

    private void loadMethods(Class<?> clazz, List<Method> postReadMethods, List<Method> preWriteMethods) {
        for (Method method : clazz.getDeclaredMethods()) {
            checkMethodAnnotation(clazz, method, PostRead.class, postReadMethods);
            checkMethodAnnotation(clazz, method, PreWrite.class, preWriteMethods);
        }
    }

    private void checkMethodAnnotation(Class<?> clazz, Method method, Class<? extends Annotation> annotation, List<Method> methodList) {
        if (method.isAnnotationPresent(annotation)) {
            if (method.getParameterTypes().length > 0) {
                throw new JfpaException(clazz, String.format("@%s method '%s'  can't have parameters", annotation.getName(), method.getName()));
            }
            method.setAccessible(true);
            methodList.add(method);
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
                    if ("?".equals(className)) {
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
            extractor = (TypeExtractor) clazz.newInstance();
            extractors.put(clazz, extractor);
        }
        return extractor;
    }

    protected final List<CachedSubRecord> getFirsts(final Class<?> clazz) {
        return multipleClasses.get(clazz).getFirsts();
    }

    protected final CachedRecord getCachedRecord(final Class<?> clazz) {
        return singleClasses.get(clazz);
    }

    protected final CachedMultipleRecord getCachedMultipleRecord(final Class<?> clazz) {
        return multipleClasses.get(clazz);
    }

    protected final Class<?> getTypeClass(RecordType recordType) {
        return typeClasses.get(recordType);
    }

    public String getDefaultDateFormat() {
        return defaultDateFormat;
    }

    public String[] getDefaultBooleanFormat() {
        return defaultBooleanFormat;
    }

}
