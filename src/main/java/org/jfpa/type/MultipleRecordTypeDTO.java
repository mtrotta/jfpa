package org.jfpa.type;

import org.jfpa.dto.MultipleFlatRecordDTO;
import org.jfpa.interfaces.MultipleFlatRecordDTOFactory;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 05/04/11
 */
public class MultipleRecordTypeDTO<T extends MultipleFlatRecordDTO> extends MultipleRecordType {

    private final MultipleFlatRecordDTOFactory<T> factory;

    public MultipleRecordTypeDTO(MultipleFlatRecordDTOFactory<T> factory) {
        this.factory = factory;
    }

    public MultipleRecordTypeDTO(String typeString, MultipleFlatRecordDTOFactory<T> factory) {
        super(typeString);
        this.factory = factory;
    }

    public MultipleFlatRecordDTOFactory<T> getFactory() {
        return factory;
    }
}
