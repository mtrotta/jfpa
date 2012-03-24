package org.jfpa.interfaces;

import org.jfpa.dto.FlatRecordDTO;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.type.RecordType;
import org.jfpa.type.RecordTypeDTO;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 22/03/11
 */
public interface FlatRecord extends Record, RecordValidator {
    String getString(int pos) throws InvalidRecordException;

    void setString(int pos, String value) throws InvalidRecordException;

    Date getDate(int pos, String format) throws InvalidRecordException;

    void setDate(int pos, Date value, String format) throws InvalidRecordException;

    Integer getInteger(int pos) throws InvalidRecordException;

    void setInteger(int pos, Integer value) throws InvalidRecordException;

    Long getLong(int pos) throws InvalidRecordException;

    void setLong(int pos, Long value) throws InvalidRecordException;

    Double getDouble(int pos) throws InvalidRecordException;

    void setDouble(int pos, Double value) throws InvalidRecordException;

    BigDecimal getBigDecimal(int pos) throws InvalidRecordException;

    void setBigDecimal(int pos, BigDecimal value) throws InvalidRecordException;

    Boolean getBoolean(int pos, String[] trueFalse) throws InvalidRecordException;

    void setBoolean(int pos, Boolean value, String[] trueFalse) throws InvalidRecordException;

    int getColumns();

    RecordType getType();

    boolean isType(RecordType... recordTypes);

    <T extends FlatRecordDTO> T createDTO(RecordTypeDTO<T> recordType);

    <T extends FlatRecordDTO> T createDTO(FlatRecordDTOFactory<T> factory);
}
