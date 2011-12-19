package org.jfpa.inheritance;

import org.jfpa.dto.FlatRecordDTO;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.interfaces.FlatRecord;
import org.jfpa.interfaces.FlatRecordDTOFactory;
import org.jfpa.record.DelimitedRecord;
import org.jfpa.type.RecordTypeDTO;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 27/10/11
 */
public class FlatRecordTest {

    @Test
    public void testInteger() throws Exception {
        FlatRecord record = new FakeDelimited();
        record.setInteger(0, 7);
        Assert.assertTrue(7 == record.getInteger(0));
        record.setDouble(1, 7.0);
        Assert.assertTrue(7.0 == record.getDouble(1));
    }

    @Test
    public void testDto() throws Exception {
        FakeDelimited record = new FakeDelimited();
        Assert.assertNotNull(record.createDTO(FakeDelimited.DEFAULT));
        Assert.assertNotNull(record.createDTO(FakeDelimited.FACTORY));
    }

    @Test(expected = InvalidRecordException.class)
    public void testBadInteger() throws Exception {
        FlatRecord record = new FakeDelimited("A;B");
        record.getInteger(0);
    }

    @Test(expected = InvalidRecordException.class)
    public void testBadLong() throws Exception {
        FlatRecord record = new FakeDelimited("A;B");
        record.getLong(0);
    }

    @Test(expected = InvalidRecordException.class)
    public void testBadDouble() throws Exception {
        FlatRecord record = new FakeDelimited("A;B");
        record.getDouble(0);
    }

    @Test(expected = InvalidRecordException.class)
    public void testBadBigDecimal() throws Exception {
        FlatRecord record = new FakeDelimited("A;B");
        record.getBigDecimal(0);
    }

    @Test(expected = InvalidRecordException.class)
    public void testBadBoolean() throws Exception {
        FlatRecord record = new FakeDelimited("A;B");
        record.getBoolean(0, new String[]{"T", "F"});
    }

    public static class FakeDelimited extends DelimitedRecord {

        public static final FlatRecordDTOFactory<FakeDTO> FACTORY = new FlatRecordDTOFactory<FakeDTO>() {
            public FakeDTO createDTO(FlatRecord record) {
                return new FakeDTO(record);
            }
        };

        public static final RecordTypeDTO<FakeDTO> DEFAULT = new RecordTypeDTO<FakeDTO>(";",2, FACTORY);

        public FakeDelimited(String str) throws InvalidRecordException {
            super(DEFAULT, str);
        }

        public FakeDelimited() throws InvalidRecordException {
            super(DEFAULT);
        }
    }

    public static class FakeDTO extends FlatRecordDTO {
        public FakeDTO(FlatRecord record) {
            super(record);
        }
    }
}
