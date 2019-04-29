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

package org.jfpa.annotatated;

import org.jfpa.annotation.Delimited;
import org.jfpa.annotation.TextColumn;
import org.jfpa.annotation.WrappedColumns;
import org.jfpa.manager.RecordManager;
import org.junit.Assert;
import org.junit.Test;

public class WrappedTest {

    private RecordManager manager = new RecordManager();

    @Test
    public void testWrappedRead() throws Exception {
        FakeDelimited record = manager.read("A;B;C", FakeDelimited.class);
        Assert.assertEquals("A",record.before);
        Assert.assertEquals("B",record.wrapped.innerColumn);
        Assert.assertEquals("C",record.after);
    }

    @Test
    public void testWrappedWrite() throws Exception {
        FakeDelimited record = new FakeDelimited();
        record.before = "A";
        record.wrapped.innerColumn = "B";
        record.after = "C";
        Assert.assertEquals("A;B;C", manager.write(record));
    }

    public static class FakeWrapped {
        @TextColumn
        public String innerColumn;

    }

    @Delimited
    public static class FakeDelimited {
        @TextColumn
        public String before;
        @WrappedColumns
        public FakeWrapped wrapped = new FakeWrapped();
        @TextColumn
        public String after;
    }

}
