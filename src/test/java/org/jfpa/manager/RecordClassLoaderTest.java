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

package org.jfpa.manager;

import org.jfpa.annotatated.*;
import org.jfpa.annotation.*;
import org.jfpa.exception.JfpaException;
import org.jfpa.interfaces.Converter;
import org.jfpa.interfaces.TypeExtractor;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class RecordClassLoaderTest {

    private RecordClassLoader recordClassLoader = new RecordClassLoader(RecordManager.DEFAULT_DATE_FORMAT, RecordManager.DEFAULT_BOOLEAN_FORMAT);

    @Positional
    public static class BadBooleanFormatNum {
        @TextColumn(length = 1, booleanFormat = {"AB","CD","EF"})
        private Boolean value;
    }

    @Test(expected = JfpaException.class)
    public void testBadBooleanFormat() throws Exception {
        recordClassLoader.loadClass(BadBooleanFormatNum.class);
    }

    @Positional
    @Delimited(delimiter = ";")
    public static class BadPositionalDelimited {
    }

    public static class Bean implements Converter {
        private String innerValue;

        public String getInnerValue() {
            return innerValue;
        }
        public String read() {
            return innerValue;
        }
        public void write(String string) {
            this.innerValue = string;
        }
    }

    @Positional
    public static class RecordConverter {
        @TextColumn(length = 3)
        private Bean value;

        public Bean getValue() {
            return value;
        }
    }

    public static class Extractor implements TypeExtractor {
        public String extractType(String line) {
            return null;
        }
    }

    @MultiplePositional(typeExtractor = Extractor.class)
    public static class FakePositionalExtractor { }

    @MultipleDelimited(typeExtractor = Extractor.class)
    public static class FakeDelimitedExtractor { }

    @Test
    public void testLoadExtractor() throws Exception {
        recordClassLoader.loadClass(FakePositionalExtractor.class);
        recordClassLoader.loadClass(FakeDelimitedExtractor.class);
    }

    @Test
    public void testNullExtractor() throws Exception {
        NullExtractor nullExtractor = new NullExtractor();
        Assert.assertNull(nullExtractor.extractType(null));
    }

    public abstract static class AbstractExtractor implements TypeExtractor {
        public String extractType(String line) {
            return null;
        }
    }

    @MultiplePositional(typeExtractor = AbstractExtractor.class)
    public static class BadAbstractExtractor { }

    @Test(expected = JfpaException.class)
    public void testBadAbstractExtractor() throws Exception {
        recordClassLoader.loadClass(BadAbstractExtractor.class);
    }

    public static class PrivateExtractor implements TypeExtractor {
        private PrivateExtractor() { }
        public String extractType(String line) {
            return null;
        }
    }

    @MultiplePositional(typeExtractor = PrivateExtractor.class)
    public static class BadPrivateExtractor { }

    @Test(expected = JfpaException.class)
    public void testBadPrivateExtractor() throws Exception {
        recordClassLoader.loadClass(BadPrivateExtractor.class);
    }

    @Positional
    public static class BadStatic {
        @TextColumn(length = 1)
        private static String value;

    }

    @Test(expected = JfpaException.class)
    public void testBadPostRead() throws Exception {
        recordClassLoader.loadClass(BadRecordPostReadMethod.class);
    }

    @Delimited(delimiter = ";")
    public static class BadRecordPostReadMethod {
        @TextColumn(length = -1, name = "COL1")
        private String col1;
        @PostRead
        private void init(String s) {
        }
    }

    @Test(expected = JfpaException.class)
    public void testBadPreWrite() throws Exception {
        recordClassLoader.loadClass(BadRecordPreWriteMethod.class);
    }

    @Delimited(delimiter = ";")
    public static class BadRecordPreWriteMethod {
        @TextColumn(length = -1, name = "COL1")
        private String col1;
        @PreWrite
        private void init(String s) {
        }
    }

    @Test
    public void testOuterExtractor() throws Exception {
        recordClassLoader.loadClass(MultiplePositionalTest.FakeMultiplePositionalRecordOuterExtractor.class);
    }

    @MultiplePositional
    public static class BadMultiplePositionalRecordNone {
    }

    @Test(expected = JfpaException.class)
    public void testFailNone() throws Exception {
        recordClassLoader.loadClass(BadMultiplePositionalRecordNone.class);
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1, typeExtractor = BadMultiplePositionalRecordBoth.class)
    public static class BadMultiplePositionalRecordBoth implements TypeExtractor {
        public String extractType(String line) {
            return null;
        }
    }

    @Test(expected = JfpaException.class)
    public void testFailBoth() throws Exception {
        recordClassLoader.loadClass(BadMultiplePositionalRecordBoth.class);
    }

    @MultiplePositional(typePositionBegin = 20)
    public static class BadMultiplePositionalRecordEndMissing {}

    @Test(expected = JfpaException.class)
    public void testEndMissing() throws Exception {
        recordClassLoader.loadClass(BadMultiplePositionalRecordEndMissing.class);
    }

    @MultiplePositional(typePositionEnd = 20)
    public static class BadMultiplePositionalRecordBeginMissing {}

    @Test(expected = JfpaException.class)
    public void testBeginMissing() throws Exception {
        recordClassLoader.loadClass(BadMultiplePositionalRecordBeginMissing.class);
    }

    @MultiplePositional(typePositionBegin = 30, typePositionEnd = 20)
    public static class BadMultiplePositionalRecordBadNumbers {}

    @Test(expected = JfpaException.class)
    public void testBadNumbers() throws Exception {
        recordClassLoader.loadClass(BadMultiplePositionalRecordBadNumbers.class);
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 0)
    public static class BadMultiplePositionalRecordNegativeNumbers {}

    @Test(expected = JfpaException.class)
    public void testNegativeNumbers() throws Exception {
        recordClassLoader.loadClass(BadMultiplePositionalRecordNegativeNumbers.class);
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public static class FakeMultiplePositionalNone {
        @SuppressWarnings("unused")
        private FakePositionalRecordA recordA;
    }

    @Test
    public void testNone() throws Exception {
        recordClassLoader.loadClass(FakeMultiplePositionalNone.class);
    }

    @Test
    public void testLength() throws Exception {
        recordClassLoader.loadClass(PositionalTest.FakePositionalLength.class);
    }

    @Positional
    public static class BadPositionalLength {
        @TextColumn(length = 0)
        private String value1;
        @TextColumn(length = 3)
        private String value2;
        @TextColumn(length = 7)
        private String value3;
    }

    @Test(expected = JfpaException.class)
    public void testBadLength() throws Exception {
        recordClassLoader.loadClass(BadPositionalLength.class);
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public static class FakeMultipleRecordRawList {
        @SubRecord(type = "F")
        private MultipleRecordManagerTest.FirstRecord first;
        @SubRecord(type = "N")
        private List normal;


    }

    @Test(expected = JfpaException.class)
    public void testRawList() throws Exception {
        recordClassLoader.loadClass(FakeMultipleRecordRawList.class);
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public static class FakeMultipleRecordWildList {
        @SubRecord(type = "F")
        private MultipleRecordManagerTest.FirstRecord first;
        @SubRecord(type = "N")
        private List<?> normal;


    }

    @Test(expected = JfpaException.class)
    public void testWildList() throws Exception {
        recordClassLoader.loadClass(FakeMultipleRecordWildList.class);
    }

    public static class BadNestedWrapped {
        @TextColumn(length = -1)
        public String innerColumn;
        @WrappedColumns
        public WrappedTest.FakeWrapped otherWrapped;

    }

    @Delimited(delimiter = ";")
    public static class BadNestedDelimited {
        @TextColumn(length = -1)
        public String column;
        @WrappedColumns
        public BadNestedWrapped wrapped = new BadNestedWrapped();
    }

    @Test(expected = JfpaException.class)
    public void testBadNested() throws Exception {
        recordClassLoader.loadClass(BadNestedDelimited.class);
    }

    @Delimited(delimiter = ";")
    public static class BadFakeDelimited {
        @TextColumn(length = -1)
        public String column;
        @WrappedColumns
        @TextColumn(length = -1)
        public BadNestedWrapped wrapped = new BadNestedWrapped();
    }

    @Test(expected = JfpaException.class)
    public void testBadColAndWrap() throws Exception {
        recordClassLoader.loadClass(BadFakeDelimited.class);
    }

    @MultipleDelimited
    public static class BadMultipleDelimitedRecordNone {
    }

    @Test(expected = JfpaException.class)
    public void testFailNoFields() throws Exception {
        recordClassLoader.loadClass(BadMultipleDelimitedRecordNone.class);
    }

    @MultipleDelimited(delimiter = ";")
    public static class BadMultipleDelimitedRecordDel {
    }

    @Test(expected = JfpaException.class)
    public void testFailDel() throws Exception {
        recordClassLoader.loadClass(BadMultipleDelimitedRecordDel.class);
    }

    @MultipleDelimited(typePosition = 0)
    public static class BadMultipleDelimitedRecordPos {
    }

    @Test(expected = JfpaException.class)
    public void testFailPos() throws Exception {
        recordClassLoader.loadClass(BadMultipleDelimitedRecordPos.class);
    }

    @MultipleDelimited(delimiter = ";", typePosition = 0, typeExtractor = BadMultipleDelimitedRecordBoth.class)
    public static class BadMultipleDelimitedRecordBoth implements TypeExtractor {
        public String extractType(String line) {
            return null;
        }
    }

    @Test(expected = JfpaException.class)
    public void testFailBothRecord() throws Exception {
        recordClassLoader.loadClass(BadMultipleDelimitedRecordBoth.class);
    }

    @MultipleDelimited(typeExtractor = BadMultipleDelimitedRecordBoth.class)
    public static class FakeMultipleDelimitedRecordOuterExtractor {
    }

    @Test
    public void testMultipleOuterExtractor() throws Exception {
        recordClassLoader.loadClass(FakeMultipleDelimitedRecordOuterExtractor.class);
    }

    @MultipleDelimited(delimiter = ";", typePosition = 0)
    public static class BadMultipleDelimitedMixed {
        @SubRecord(type = "A")
        private FakeDelimitedRecordA recordA;
        @SubRecord(type = "B")
        private FakePositionalRecordB recordB;
    }

    @Test(expected = JfpaException.class)
    public void testBadMixed() throws Exception {
        recordClassLoader.loadClass(BadMultipleDelimitedMixed.class);
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public static class BadMultipleDelimitedMixedElse {
        @SubRecord(type = "A")
        private FakePositionalRecordA recordA;
        @SubRecord(type = "B")
        private FakeDelimitedRecordB recordB;
    }

    @Test(expected = JfpaException.class)
    public void testBadMixedElse() throws Exception {
        recordClassLoader.loadClass(BadMultipleDelimitedMixedElse.class);
    }


    @Delimited(delimiter = ";")
    public static class FakeDelimitedLength {
        @TextColumn(length = 3)
        private String value1;
        @TextColumn(length = 3)
        private String value2;
        @TextColumn(length = 5)
        private String value3;

        public void setValue2(String value2) {
            this.value2 = value2;
        }
    }

    @Test
    public void testDelimitedLength() throws Exception {
        recordClassLoader.loadClass(FakeDelimitedLength.class);
    }

    @Delimited(delimiter = "")
    public static class FakeDelimitedBad {}

    @Test(expected = JfpaException.class)
    public void testBad() throws Exception {
        recordClassLoader.loadClass(FakeDelimitedBad.class);
    }

}
