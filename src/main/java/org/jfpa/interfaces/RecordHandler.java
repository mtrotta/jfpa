package org.jfpa.interfaces;

import org.jfpa.exception.InvalidRecordException;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 24/03/11
 */
public interface RecordHandler<T> {
    void handle(T record) throws InvalidRecordException;
}
