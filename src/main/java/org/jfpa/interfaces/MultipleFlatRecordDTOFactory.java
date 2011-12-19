package org.jfpa.interfaces;

import org.jfpa.dto.MultipleFlatRecordDTO;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 25/03/11
 */
public interface MultipleFlatRecordDTOFactory<T extends MultipleFlatRecordDTO> {

    T createDTO(MultipleFlatRecord record);

}
