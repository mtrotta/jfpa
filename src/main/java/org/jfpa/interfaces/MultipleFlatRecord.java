package org.jfpa.interfaces;

import org.jfpa.dto.FlatRecordDTO;
import org.jfpa.dto.MultipleFlatRecordDTO;
import org.jfpa.exception.InvalidMultipleRecordException;
import org.jfpa.type.MultipleRecordType;
import org.jfpa.type.MultipleRecordTypeDTO;
import org.jfpa.type.RecordType;
import org.jfpa.type.RecordTypeDTO;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 24/03/11
 */
public interface MultipleFlatRecord extends Record, MultipleRecordValidator {
    int size();

    boolean isEmpty();

    void clear();

    boolean isComplete();

    MultipleRecordType getType() throws InvalidMultipleRecordException;

    boolean isType(MultipleRecordType... types);

    boolean contains(RecordType... types);

    boolean containsAll(RecordType... types);

    FlatRecord findFirstRecord(RecordType... types);

    List<FlatRecord> findRecords(RecordType... types);

    Map<RecordType, List<FlatRecord>> getRecords();

    Set<RecordType> getAllTypes();

    void addRecord(FlatRecord record);

    boolean isFirst(FlatRecord record);

    <T extends FlatRecordDTO> List<T> createFlatRecordDTOList(RecordTypeDTO<T> recordType);

    <T extends FlatRecordDTO> T createFlatRecordDTO(RecordTypeDTO<T> recordType);

    <T extends MultipleFlatRecordDTO> T createDTO(MultipleRecordTypeDTO<T> recordType);

    <T extends MultipleFlatRecordDTO> T createDTO(MultipleFlatRecordDTOFactory<T> factory);
}
