package org.jfpa.cache;

import org.jfpa.interfaces.TypeExtractor;
import org.jfpa.manager.SeparatorType;
import org.jfpa.type.RecordType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 01/10/11
 */
public class CachedMultipleRecord {
    private SeparatorType separatorType;
    private final Map<String, RecordType> mapTypes;
    private final Map<RecordType, CachedSubRecord> mapFields;
    private int typePositionBegin;
    private int typePositionEnd;
    private String delimiter;
    private int typePosition;
    private TypeExtractor typeExtractor;
    private boolean validator;
    private final List<CachedSubRecord> firsts;

    public CachedMultipleRecord(final Map<String, RecordType> mapTypes, final Map<RecordType, CachedSubRecord> mapFields, final List<CachedSubRecord> firsts) {
        this.mapTypes = mapTypes;
        this.mapFields = mapFields;
        this.firsts = firsts;
    }

    public final void setPositional(final TypeExtractor typeExtractor) {
        this.separatorType = SeparatorType.POSITIONAL;
        this.typeExtractor = typeExtractor;
    }

    public final void setPositional(final int typePositionBegin, final int typePositionEnd) {
        this.separatorType = SeparatorType.POSITIONAL;
        this.typePositionBegin = typePositionBegin;
        this.typePositionEnd = typePositionEnd;
    }

    public final void setDelimited(final String delimiter, final int typePosition) {
        this.separatorType = SeparatorType.DELIMITED;
        this.delimiter = Pattern.quote(delimiter);
        this.typePosition = typePosition;
    }

    public final void setDelimited(final TypeExtractor typeExtractor) {
        this.separatorType = SeparatorType.DELIMITED;
        this.typeExtractor = typeExtractor;
    }

    public final SeparatorType getSeparatorType() {
        return separatorType;
    }

    public final int getTypePositionEnd() {
        return typePositionEnd;
    }

    public final String getDelimiter() {
        return delimiter;
    }

    public final int getTypePosition() {
        return typePosition;
    }

    public final RecordType getType(final String type) {
        return mapTypes.get(type);
    }

    public final CachedSubRecord getCachedSubRecord(final RecordType recordType) {
        return mapFields.get(recordType);
    }

    public final Collection<CachedSubRecord> getAllFields() {
        return mapFields.values();
    }

    public final TypeExtractor getTypeExtractor() {
        return typeExtractor;
    }

    public final int getTypePositionBegin() {
        return typePositionBegin;
    }

    public final boolean isValidator() {
        return validator;
    }

    public final void setValidator(final boolean validator) {
        this.validator = validator;
    }

    public final List<CachedSubRecord> getFirsts() {
        return firsts;
    }
}
