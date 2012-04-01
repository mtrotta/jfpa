package org.jfpa.record;

import org.jfpa.dto.FlatRecordDTO;
import org.jfpa.interfaces.FlatRecordDTOFactory;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.interfaces.FlatRecord;
import org.jfpa.type.RecordType;
import org.jfpa.type.RecordTypeDTO;
import org.jfpa.utility.Utility;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 23/03/11
 */
public abstract class AbstractRecord implements FlatRecord {

    private RecordType recordType;

    public String getString(int pos) throws InvalidRecordException {
        try {
            return Utility.trimString(getPos(pos));
        } catch (IllegalArgumentException e) {
            throw new InvalidRecordException("Invalid String: " + e.getMessage(), toString());
        }
    }

    public void setString(int pos, String value) throws InvalidRecordException {
        try {
            setPos(pos, value);
        } catch (IllegalArgumentException e) {
            throw new InvalidRecordException("Invalid String: " + e.getMessage(), toString());
        }
    }

    public Date getDate(int pos, String format) throws InvalidRecordException {
        try {
            return Utility.stringToDate(getPos(pos), format);
        } catch (IllegalArgumentException e) {
            throw new InvalidRecordException("Invalid Date format: " + e.getMessage(), toString());
        }
    }

    public void setDate(int pos, Date value, String format) throws InvalidRecordException {
        try {
            setPos(pos, Utility.dateToString(value, format));
        } catch (IllegalArgumentException e) {
            throw new InvalidRecordException("Invalid Date format: " + e.getMessage(), toString());
        }
    }

    public Integer getInteger(int pos) throws InvalidRecordException {
        try {
            return Utility.stringToInteger(getPos(pos));
        } catch (IllegalArgumentException e) {
            throw new InvalidRecordException("Invalid Integer format: " + e.getMessage(), toString());
        }
    }

    public void setInteger(int pos, Integer value) throws InvalidRecordException {
        setPos(pos, Utility.integerToString(value));
    }

    public Long getLong(int pos) throws InvalidRecordException {
        try {
            return Utility.stringToLong(getPos(pos));
        } catch (IllegalArgumentException e) {
            throw new InvalidRecordException("Invalid Long format: " + e.getMessage(), toString());
        }
    }

    public void setLong(int pos, Long value) throws InvalidRecordException {
        setPos(pos, Utility.longToString(value));
    }

    public Double getDouble(int pos) throws InvalidRecordException {
        try {
            return Utility.stringToDouble(getPos(pos));
        } catch (IllegalArgumentException e) {
            throw new InvalidRecordException("Invalid Double format: " + e.getMessage(), toString());
        }
    }

    public void setDouble(int pos, Double value) throws InvalidRecordException {
        setPos(pos, Utility.doubleToString(value));
    }

    public BigDecimal getBigDecimal(int pos) throws InvalidRecordException {
        try {
            return Utility.stringToBigDecimal(getPos(pos));
        } catch (IllegalArgumentException e) {
            throw new InvalidRecordException("Invalid BigDecimal format: " + e.getMessage(), toString());
        }
    }

    public void setBigDecimal(int pos, BigDecimal value) throws InvalidRecordException {
        setPos(pos, Utility.bigDecimalToString(value));
    }

    public void setBoolean(int pos, Boolean value, String[] trueFalse) throws InvalidRecordException {
        setPos(pos, Utility.booleanToString(value, trueFalse));
    }

    public Boolean getBoolean(int pos, String[] trueFalse) throws InvalidRecordException {
        try {
            return Utility.stringToBoolean(getPos(pos), trueFalse);
        } catch (IllegalArgumentException e) {
            throw new InvalidRecordException("Invalid Boolean format: " + e.getMessage(), toString());
        }
    }

    public RecordType getType() {
        return recordType;
    }

    protected void setRecordType(RecordType recordType) {
        this.recordType = recordType;
    }

    public boolean isType(RecordType... types) {
        return Utility.checkDomain(getType(), types);
    }

    public <T extends FlatRecordDTO> T createDTO(RecordTypeDTO<T> recordType) {
        return recordType.getFactory().createDTO(this);
    }

    public <T extends FlatRecordDTO> T createDTO(FlatRecordDTOFactory<T> factory) {
        return factory.createDTO(this);
    }

    protected void checkString(String string) throws InvalidRecordException {
        if (string == null) {
            throw new InvalidRecordException("Record string is null");
        }
    }

    public void validate() throws InvalidRecordException { }

    protected abstract String getPos(int pos) throws InvalidRecordException;
    protected abstract void setPos(int pos, String value) throws InvalidRecordException;
}
