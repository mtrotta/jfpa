package org.jfpa.interfaces;

import org.jfpa.dto.FlatRecordDTO;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 25/03/11
 */
public interface FlatRecordDTOFactory<T extends FlatRecordDTO> {

    T createDTO(FlatRecord record);

}
