/*
 * Copyright (c) 2011 Matteo Trotta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jfpa.builder;

import org.jfpa.exception.InvalidMultipleRecordException;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.interfaces.FlatRecord;
import org.jfpa.interfaces.MultipleFlatRecord;
import org.jfpa.interfaces.RecordHandler;

public class MultipleRecordBuilder<T extends MultipleFlatRecord> {

    private final T record;
    private final RecordHandler<T> handler;

    public MultipleRecordBuilder(final T record, final RecordHandler<T> handler) {
        this.record = record;
        this.handler = handler;
    }

    public final void add(final FlatRecord flatRecord) throws InvalidRecordException {
        if (record.isFirst(flatRecord)) {
            try {
                flush();
            } finally {
                record.addRecord(flatRecord);
            }
        } else if (record.isEmpty()) {
            throw new InvalidMultipleRecordException("Out of sync: missing header for record '" + flatRecord + "'", record.toString());
        } else {
            record.addRecord(flatRecord);
        }
    }

    public final void flush() throws InvalidRecordException {
        try {
            if (!record.isEmpty()) {
                record.validate();
                handler.handle(record);
            }
        } finally {
            record.clear();
        }
    }
}
