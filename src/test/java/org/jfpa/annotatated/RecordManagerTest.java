package org.jfpa.annotatated;

import org.jfpa.annotation.*;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.exception.JfpaException;
import org.jfpa.interfaces.Converter;
import org.jfpa.utility.Formats;
import org.jfpa.interfaces.TypeExtractor;
import org.jfpa.manager.NullExtractor;
import org.jfpa.manager.RecordManager;
import org.jfpa.utility.Utility;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 09/10/11
 */
public class RecordManagerTest {

    private RecordManager manager = new RecordManager();

    @Test
    public void testOk() throws Exception {
        SimpleString simpleString = new SimpleString();
        String value = "1";
        simpleString.setValue(value);
        Assert.assertEquals(value, manager.read(value, SimpleString.class).getValue());
        Assert.assertEquals(value, manager.write(simpleString));
    }

    @Test
    public void testFormats() throws Exception {
        RecordManager manager = new RecordManager(Formats.DATE_FORMAT_INVERTED, Formats.BOOLEAN_TRUE_FALSE);

        String inverted = Utility.dateToString(Common.testDate, Formats.DATE_FORMAT_INVERTED);
        SimpleDate simpleDate = manager.read(inverted, SimpleDate.class);
        Assert.assertEquals(Common.testDate, simpleDate.getValue());
        Assert.assertEquals(inverted, manager.write(simpleDate));

        String falseVal = Formats.BOOLEAN_TRUE_FALSE[1];
        SimpleBoolean simpleBoolean = manager.read(falseVal, SimpleBoolean.class);
        Assert.assertEquals(Boolean.FALSE, simpleBoolean.getValue());
        Assert.assertEquals(falseVal, manager.write(simpleBoolean));
    }

    @Positional
    public static class SimpleString {
        @TextColumn(length = 1)
        private String value;

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }
    }

    @Test
    public void testString() throws Exception {
        String value = "A";
        Assert.assertEquals(value, manager.read(value, SimpleString.class).getValue());
    }

    @Positional
    public static class SimpleDate {
        @TextColumn(length = 10)
        private Date value;

        public Date getValue() {
            return value;
        }
    }

    @Test
    public void testDate() throws Exception {
        Date value = Common.testDate;
        Assert.assertEquals(value, manager.read(Utility.dateToString(value, manager.getDefaultDateFormat()), SimpleDate.class).getValue());
    }

    @Positional
    public static class SimpleDateBad {
        @TextColumn(length = 10)
        private Date badValue;
        @TextColumn(length = 10, invalidateOnError = false, dateFormat = "ABC")
        private Date badValueNotSoBad;

        public void setBadValueNotSoBad(Date badValueNotSoBad) {
            this.badValueNotSoBad = badValueNotSoBad;
        }
    }

    @Test
    public void testBadDate() throws Exception {
        SimpleDateBad simpleDateBad = manager.read("          1234567890", SimpleDateBad.class);
        try {
            manager.read("1234567890          ", SimpleDateBad.class);
            Assert.fail();
        } catch (InvalidRecordException ignore) {}
        simpleDateBad.setBadValueNotSoBad(new Date());
        manager.write(simpleDateBad);
    }

    @Positional
    public static class SimpleInteger {
        @TextColumn(length = 2)
        private Integer value;

        public Integer getValue() {
            return value;
        }

        public void setValue(Integer value) {
            this.value = value;
        }
    }

    @Test
    public void testInteger() throws Exception {
        Integer value = 50;
        String stringValue = "50";
        Assert.assertEquals(value, manager.read(stringValue, SimpleInteger.class).getValue());
        SimpleInteger simpleInteger = new SimpleInteger();
        simpleInteger.setValue(value);
        Assert.assertEquals(stringValue, manager.write(simpleInteger));
    }

    @Positional
    public static class SimpleLong {
        @TextColumn(length = 2)
        private Long value;

        public Long getValue() {
            return value;
        }
    }

    @Test
    public void testLong() throws Exception {
        Long value = 50L;
        Assert.assertEquals(value, manager.read("50", SimpleLong.class).getValue());
    }

    @Positional
    public static class SimpleDouble {
        @TextColumn(length = 4)
        private Double value;

        public Double getValue() {
            return value;
        }

        public void setValue(Double value) {
            this.value = value;
        }
    }

    @Test
    public void testDouble() throws Exception {
        Double value = 50.3;
        String stringValue = "50.3";
        Assert.assertEquals(value, manager.read(stringValue, SimpleDouble.class).getValue());
        SimpleDouble simpleDouble = new SimpleDouble();
        simpleDouble.setValue(value);
        Assert.assertEquals(stringValue, manager.write(simpleDouble));
    }

    @Positional
    public static class SimpleBigDecimal {
        @TextColumn(length = 4)
        private BigDecimal value;

        public BigDecimal getValue() {
            return value;
        }
    }

    @Test
    public void testBigDecimal() throws Exception {
        String str = "50.3";
        BigDecimal value = Utility.stringToBigDecimal(str);
        Assert.assertEquals(value, manager.read(str, SimpleBigDecimal.class).getValue());
    }

    @Positional
    public static class SimpleBoolean {
        @TextColumn(length = 5)
        private Boolean value;

        public Boolean getValue() {
            return value;
        }
    }

    @Test
    public void testBoolean() throws Exception {
        Boolean value = Boolean.TRUE;
        Assert.assertEquals(value, manager.read(manager.getDefaultBooleanFormat()[0]+"    ", SimpleBoolean.class).getValue());
    }

    public static class Bad {}

    @Test(expected = JfpaException.class)
    public void testBadE() throws Exception {
        manager.read("", Bad.class);
    }

    @Test(expected = JfpaException.class)
    public void testBadW() throws Exception {
        manager.write(new Bad());
    }

    @Test(expected = InvalidRecordException.class)
    public void testReadNull() throws Exception {
        manager.read(null, SimpleString.class);
    }

    @Delimited(delimiter = ";")
    public static class SimpleDelimited {
        @TextColumn(length = -1)
        private String value;
    }

    @Test(expected = InvalidRecordException.class)
    public void testReadNullDelimited() throws Exception {
        manager.read(null, SimpleDelimited.class);
    }

    @Test(expected = NullPointerException.class)
    public void testWriteNull() throws Exception {
        manager.write(null);
    }

    @Positional
    public static class BadType {
        @TextColumn(length = 1)
        private Bad value;
    }

    @Test(expected = JfpaException.class)
    public void testBadType() throws Exception {
        manager.write(new BadType());
    }

    @Positional
    public static class BadBooleanFormat {
        @TextColumn(length = 1, dateFormat = "AB")
        private Boolean value;
    }

    @Test(expected = JfpaException.class)
    public void testBadBooleanType() throws Exception {
        manager.write(new BadBooleanFormat());
    }

    @Positional
    public static class BadBooleanFormatNum {
        @TextColumn(length = 1, booleanFormat = {"AB","CD","EF"})
        private Boolean value;
    }

    @Test(expected = JfpaException.class)
    public void testBadBooleanFormat() throws Exception {
        manager.loadClass(BadBooleanFormatNum.class);
    }

    @Positional
    @Delimited(delimiter = ";")
    public static class BadPositionalDelimited {
    }

    @Test(expected = JfpaException.class)
    public void testBadPositionalDelimited() throws Exception {
        manager.write(new BadPositionalDelimited());
    }

    @Positional
    @MultiplePositional
    public static class BadPositionalVariable {
    }

    @Test(expected = JfpaException.class)
    public void testBadPositionalVariable() throws Exception {
        manager.write(new BadPositionalVariable());
    }

    @Positional
    public static class RecordValidator implements org.jfpa.interfaces.RecordValidator {
        public void validate() throws InvalidRecordException {
            throw new InvalidRecordException("Invalid");
        }
    }

    @Test(expected = InvalidRecordException.class)
    public void testValidatorRead() throws Exception {
        manager.read("ABC", RecordValidator.class);
    }

    @Test(expected = InvalidRecordException.class)
    public void testValidatorWrite() throws Exception {
        manager.write(new RecordValidator());
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

    @Test
    public void testConverter() throws Exception {
        String value = "ABC";
        RecordConverter recordConverter = manager.read(value, RecordConverter.class);
        Assert.assertEquals(value, recordConverter.getValue().getInnerValue());
        Assert.assertEquals(value, manager.write(recordConverter));
    }

    @Positional
    public static class BadConstructor {
        private BadConstructor(){}
    }

    @Test(expected = JfpaException.class)
    public void testBadConstructor() throws Exception {
        manager.read(" ", BadConstructor.class);
    }

    @Positional
    public static class BadConstructorException {
        public BadConstructorException() {
            throw new RuntimeException("Simulated Exception");
        }
    }

    @Test(expected = RuntimeException.class)
    public void testBadConstructorException() throws Exception {
        manager.read(" ", BadConstructorException.class);
    }

    @Positional
    public abstract static class BadAbstract {
    }

    @Test(expected = JfpaException.class)
    public void testBadAbstract() throws Exception {
        manager.read("", BadAbstract.class);
    }

    @MultiplePositional(typePositionBegin = 0, typePositionEnd = 1)
    public abstract static class BadAbstractMultiple {
    }

    @Test(expected = JfpaException.class)
    public void testBadAbstractMultiple() throws Exception {
        manager.read("", BadAbstractMultiple.class);
    }

    @Test(expected = NullPointerException.class)
    public void testNull() throws Exception {
        manager.read(null, null);
    }

    @Positional
    public static class FakePositionalTransient {
        @TextColumn(length = 3)
        private String val;

        private String otherVal;

        public String getOtherVal() {
            return otherVal;
        }

        public void setOtherVal(String otherVal) {
            this.otherVal = otherVal;
        }
    }

    @Test
    public void testTransient() throws Exception {
        String val = "Other";
        FakePositionalTransient record = new FakePositionalTransient();
        record.setOtherVal(val);
        manager.write(record);
        Assert.assertEquals(val,  record.getOtherVal());
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
        manager.loadClass(FakePositionalExtractor.class);
        manager.loadClass(FakeDelimitedExtractor.class);
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
        manager.loadClass(BadAbstractExtractor.class);
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
        manager.loadClass(BadPrivateExtractor.class);
    }

    @Positional
    public static class BadStatic {
        @TextColumn(length = 1)
        private static String value;

    }

    @Test
    public void testStatic() throws Exception {
        String value = "A";
        manager.read(value, BadStatic.class);
        Assert.assertEquals(value, BadStatic.value);
    }

}
