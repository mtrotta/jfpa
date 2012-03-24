/*
 * Copyright (c) 2012 Matteo Trotta
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

import org.jfpa.exception.InvalidRecordException;
import org.jfpa.interfaces.RecordHandler;
import org.jfpa.record.AbstractBinaryRecord;
import org.jfpa.utility.Utility;

public class BinaryRecordBuilder<T extends AbstractBinaryRecord> {

    private static final int DEFAULT_BUFFER_SIZE = 256;
    private byte[] buffer;

    private final byte[] pattern;
    private final int[] failure;
    private final RecordHandler<T> handler;
    private T record;
    private int size = 0;

    public BinaryRecordBuilder(final T record, final RecordHandler<T> handler) {
        this.buffer = new byte[DEFAULT_BUFFER_SIZE];
        this.record = record;
        this.pattern = record.getPattern();
        this.failure = computeFailure(pattern);
        this.handler = handler;
    }

    public final void process(final byte[] bytes) throws InvalidRecordException {
        int dataSize = bytes.length + size;
        if (dataSize > buffer.length) {
            increaseBufferSize(dataSize);
        }
        fillBuffer(bytes);
        processStream(false);
    }

    private void processStream(final boolean flush) throws InvalidRecordException {
        int begin = indexOf(buffer, 0);
        if (begin >= 0) {
            if (begin != 0) {
                byte[] junk = new byte[begin];
                System.arraycopy(buffer, 0, junk, 0, begin);
                freeBuffer(junk.length);
                record.setBytes(junk);
                throw new InvalidRecordException("Out of sync: " + Utility.hexString(junk), record.toString());
            }
            int end;
            while ((end = indexOf(buffer, begin + pattern.length)) > 0) {
                newRecord(begin, end);
                begin = end;
            }
            if (flush) {
                try {
                    newRecord(begin, size);
                } finally {
                    size = 0;
                }
            } else {
                freeBuffer(begin);
            }
        }
    }

    public final void flush() throws InvalidRecordException {
        processStream(true);
    }

    private void newRecord(final int begin, final int end) throws InvalidRecordException {
        int recordSize = end - begin;
        byte[] data = new byte[recordSize];
        System.arraycopy(buffer, begin, data, 0, recordSize);
        record.buildRecord(data);
        handler.handle(record);
    }

    private void fillBuffer(final byte[] src) {
        System.arraycopy(src, 0, buffer, size, src.length);
        size += src.length;
    }

    private void freeBuffer(final int length) {
        size -= length;
        System.arraycopy(buffer, length, buffer, 0, size);
    }

    private void increaseBufferSize(final int requestedSize) {
        byte[] oldBuffer = buffer;
        buffer = new byte[(int) (DEFAULT_BUFFER_SIZE * (Math.ceil((double) requestedSize / DEFAULT_BUFFER_SIZE)))];
        System.arraycopy(oldBuffer, 0, buffer, 0, oldBuffer.length);
    }

    /*
     * Finds the first occurrence of the pattern in the text.
     */
    private int indexOf(final byte[] data, final int offset) {
        int j = 0;
        for (int i = offset; i < size; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == pattern.length) {
                return i - pattern.length + 1;
            }
        }
        return -1;
    }

    /*
     * Computes the failure function using a boot-strapping process,
     * where the pattern is matched against itself.
     */
    private int[] computeFailure(final byte[] pattern) {
        int[] failure = new int[pattern.length];
        int j = 0;
        for (int i = 1; i < pattern.length; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }
        return failure;
    }

    public final boolean isEmpty() {
        return size == 0;
    }
}
