package org.jfpa.interfaces;

import org.jfpa.exception.InvalidMultipleRecordException;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 09/10/11
 */
public interface MultipleRecordValidator {
    void validate() throws InvalidMultipleRecordException;
}
