package org.jfpa.record;

import org.jfpa.dto.FlatRecordDTO;
import org.jfpa.dto.MultipleFlatRecordDTO;
import org.jfpa.exception.InvalidMultipleRecordException;
import org.jfpa.interfaces.FlatRecord;
import org.jfpa.interfaces.MultipleFlatRecord;
import org.jfpa.interfaces.MultipleFlatRecordDTOFactory;
import org.jfpa.type.MultipleRecordType;
import org.jfpa.type.MultipleRecordTypeDTO;
import org.jfpa.type.RecordType;
import org.jfpa.type.RecordTypeDTO;
import org.jfpa.utility.Utility;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 31/03/11
 */
public abstract class AbstractMultipleRecord implements MultipleFlatRecord {

    private Map<RecordType, List<FlatRecord>> records = new LinkedHashMap<RecordType, List<FlatRecord>>();

    public final void addRecord(FlatRecord record) {
        List<FlatRecord> list = records.get(record.getType());
        if (list == null) {
            records.put(record.getType(), list = new ArrayList<FlatRecord>());
        }
        list.add(record);
    }

    public final Map<RecordType, List<FlatRecord>> getRecords() {
        return records;
    }

    public final int size() {
        int size = 0;
        for (List<FlatRecord> list : records.values()) {
            size += list.size();
        }
        return size;
    }

    public final boolean isEmpty() {
        return records.isEmpty();
    }

    public final void clear() {
        records.clear();
    }

    public final boolean contains(RecordType... types) {
        for (RecordType type : types) {
            if (records.containsKey(type)) {
                return true;
            }
        }
        return false;
    }

    public boolean isComplete() {
        return records.size() > 0;
    }

    public final FlatRecord findFirstRecord(RecordType... types) {
        for (RecordType type : types) {
            if (records.containsKey(type)) {
                return records.get(type).get(0);
            }
        }
        return null;
    }

    public final List<FlatRecord> findRecords(RecordType... types) {
        List<FlatRecord> list = new ArrayList<FlatRecord>();
        for (RecordType type : types) {
            list.addAll(records.get(type));
        }
        return list;
    }

    public final Set<RecordType> getAllTypes() {
        return records.keySet();
    }

    public void validate() throws InvalidMultipleRecordException {
        if (!isComplete()) {
            invalidateRecord("record is not complete");
        }
    }

    protected final void invalidateRecord(String message) throws InvalidMultipleRecordException {
        throw new InvalidMultipleRecordException("Record didn't pass validation: " + message, toString());
    }

    public final boolean isType(MultipleRecordType... types) {
        try {
            return Utility.checkDomain(getType(), types);
        } catch (InvalidMultipleRecordException e) {
            return false;
        }
    }

    public final boolean containsAll(RecordType... types) {
        for (RecordType type : types) {
            if (!contains(type)) {
                return false;
            }
        }
        return true;
    }

    public final <T extends FlatRecordDTO> T createFlatRecordDTO(RecordTypeDTO<T> recordType) {
        FlatRecord record = findFirstRecord(recordType);
        return record != null ? record.createDTO(recordType) : null;
    }

    public final <T extends FlatRecordDTO> List<T> createFlatRecordDTOList(RecordTypeDTO<T> type) {
        List<T> list = new ArrayList<T>();
        list.add(this.createFlatRecordDTO(type));
        return list;
    }

    public final <T extends MultipleFlatRecordDTO> T createDTO(MultipleRecordTypeDTO<T> recordType) {
        return recordType.getFactory().createDTO(this);
    }

    public final <T extends MultipleFlatRecordDTO> T createDTO(MultipleFlatRecordDTOFactory<T> factory) {
        return factory.createDTO(this);
    }

    public MultipleRecordType getType() throws InvalidMultipleRecordException {
        return null;
    }

    @Override
    public String toString() {
        return Utility.buildNewLineStringFromList(records.values());
    }
}
