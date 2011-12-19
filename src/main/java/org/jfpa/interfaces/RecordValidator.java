package org.jfpa.interfaces;

import org.jfpa.exception.InvalidRecordException;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 09/10/11
 */
public interface RecordValidator {
    void validate() throws InvalidRecordException;
}
