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

import junit.framework.Assert;
import org.jfpa.annotation.Delimited;
import org.jfpa.annotation.MultipleDelimited;
import org.jfpa.annotation.Positional;
import org.jfpa.annotation.TextColumn;
import org.jfpa.exception.JfpaException;
import org.jfpa.manager.RecordManager;
import org.junit.Test;

public class HeaderTest {

    private RecordManager manager = new RecordManager();

    @Test
    public void testDelimited() throws Exception {
        Assert.assertEquals("COL1;COL2;COL3", manager.writeHeader(HeaderDelimited.class));
    }

    @Delimited(delimiter = ";")
    public static class HeaderDelimited {
        @TextColumn(length = -1, description = "COL1")
        private String value1;
        @TextColumn(length = -1, description = "COL2")
        private String value2;
        @TextColumn(length = -1, description = "COL3")
        private String value3;
    }

    @Test
    public void testPositional() throws Exception {
        Assert.assertEquals("COL1COL2COL3", manager.writeHeader(HeaderPositional.class));
    }

    @Positional
    public static class HeaderPositional {
        @TextColumn(length = 4, description = "COL1")
        private String value1;
        @TextColumn(length = 4, description = "COL2")
        private String value2;
        @TextColumn(length = 4, description = "COL3")
        private String value3;
    }

    @MultipleDelimited(delimiter = ";", typePosition = 0)
    public static class FakeMultipleDelimitedRecord {
    }

    @Test(expected = JfpaException.class)
    public void testMultiple() throws Exception {
        manager.writeHeader(FakeMultipleDelimitedRecord.class);
    }
}
