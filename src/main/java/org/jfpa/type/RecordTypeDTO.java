package org.jfpa.type;

import org.jfpa.dto.FlatRecordDTO;
import org.jfpa.interfaces.FlatRecordDTOFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 05/04/11
 */
public class RecordTypeDTO<T extends FlatRecordDTO> extends RecordType {

    private final FlatRecordDTOFactory<T> factory;

    public RecordTypeDTO(int[] lengths, FlatRecordDTOFactory<T> factory) {
        super(lengths);
        this.factory = factory;
    }

    public RecordTypeDTO(String typeString, int[] lengths, FlatRecordDTOFactory<T> factory) {
        super(typeString, lengths);
        this.factory = factory;
    }

    public RecordTypeDTO(String delimiter, int fieldNumber, FlatRecordDTOFactory<T> factory) {
        super(delimiter, fieldNumber);
        this.factory = factory;
    }

    public RecordTypeDTO(String delimiter, int fieldNumber, String stringEnclose, FlatRecordDTOFactory<T> factory) {
        super(delimiter, fieldNumber, stringEnclose);
        this.factory = factory;
    }

    public RecordTypeDTO(String typeString, String delimiter, int fieldNumber, FlatRecordDTOFactory<T> factory) {
        super(typeString, delimiter, fieldNumber);
        this.factory = factory;
    }

    public RecordTypeDTO(String typeString, String delimiter, int fieldNumber, String stringEnclose, FlatRecordDTOFactory<T> factory) {
        super(typeString, delimiter, fieldNumber, stringEnclose);
        this.factory = factory;
    }

    public FlatRecordDTOFactory<T> getFactory() {
        return factory;
    }
}
