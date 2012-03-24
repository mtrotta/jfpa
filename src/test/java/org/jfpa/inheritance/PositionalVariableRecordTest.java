package org.jfpa.inheritance;

import org.jfpa.dto.FlatRecordDTO;
import org.jfpa.interfaces.FlatRecordDTOFactory;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.interfaces.FlatRecord;
import org.jfpa.utility.Formats;
import org.jfpa.record.AbstractPositionalVariableRecord;
import org.jfpa.type.RecordType;
import org.jfpa.type.RecordTypeDTO;
import org.jfpa.utility.Utility;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 22/03/11
 */
public class PositionalVariableRecordTest {

    @Test
    public void testHeader() throws Exception {
        FlatRecord record = new FakePositionalVariableRecord(Utility.rightPad(FakePositionalVariableRecord.HEADER.getTypeString(), 17));
        Assert.assertTrue(record.isType(FakePositionalVariableRecord.HEADER));
        Assert.assertFalse(record.isType(FakePositionalVariableRecord.NORMAL));
        Assert.assertFalse(record.isType(FakePositionalVariableRecord.FOOTER));
        record.setString(1, "B");
        record.setString(2, "22/03/2011");
        Assert.assertEquals(FakePositionalVariableRecord.HEADER.getTypeString(), record.getString(0));
        Assert.assertEquals("B", record.getString(1));
        Assert.assertEquals("22/03/2011", record.getString(2));
        Assert.assertEquals("01B    22/03/2011", record.toString());
        Assert.assertEquals(Utility.stringToDate("22/03/2011", Formats.DATE_FORMAT), record.getDate(2, Formats.DATE_FORMAT));
        record.setBigDecimal(2, BigDecimal.ZERO);
        record.validate();
    }

    @Test
    public void testNormal() throws Exception {
        FlatRecord record = new FakePositionalVariableRecord(Utility.rightPad(FakePositionalVariableRecord.NORMAL.getTypeString(), 37));
        Assert.assertFalse(record.isType(FakePositionalVariableRecord.HEADER));
        Assert.assertTrue(record.isType(FakePositionalVariableRecord.NORMAL));
        Assert.assertFalse(record.isType(FakePositionalVariableRecord.FOOTER));
        record.setString(1, "B");
        record.setString(2, "22/03/2011");
        Assert.assertEquals("02B    22/03/2011                    ", record.toString());
        record.setBigDecimal(3, BigDecimal.TEN);
        Assert.assertEquals(BigDecimal.TEN, record.getBigDecimal(3));
        record.setLong(2, 0L);
        Assert.assertEquals("02B    0         10                  ", record.toString());
        Assert.assertEquals(new Long(0L), record.getLong(2));
        record.validate();
    }

    @Test
    public void testFooter() throws Exception {
        FlatRecord record = new FakePositionalVariableRecord(Utility.rightPad(FakePositionalVariableRecord.FOOTER.getTypeString(), 17));
        Assert.assertFalse(record.isType(FakePositionalVariableRecord.HEADER));
        Assert.assertFalse(record.isType(FakePositionalVariableRecord.NORMAL));
        Assert.assertTrue(record.isType(FakePositionalVariableRecord.FOOTER));
        record.setString(1, "B");
        record.setString(2, "22/03/2011");
        Assert.assertEquals("03B    22/03/2011", record.toString());
        record.setLong(2, 0L);
        Assert.assertEquals("03B    0         ", record.toString());
        Assert.assertEquals(new Long(0L), record.getLong(2));
        record.validate();
    }

    @Test(expected = InvalidRecordException.class)
    public void testInvalidType() throws Exception {
        new FakePositionalVariableRecord("04");
    }

    @Test
    public void testBasic() throws Exception {
        String line = "01               ";
        FakePositionalVariableRecord record = new FakePositionalVariableRecord(FakePositionalVariableRecord.HEADER, line);
        Assert.assertEquals(line, record.toString());
    }

    @Test
    public void testDTO() throws Exception {
        FlatRecord recordHeader = new FakePositionalVariableRecord(Utility.rightPad(FakePositionalVariableRecord.HEADER.getTypeString(), 17));
        String name = "Name";
        recordHeader.setString(1, name);
        FakeHeaderDTO dtoHeader = recordHeader.createDTO(FakePositionalVariableRecord.HEADER);
        Assert.assertEquals(name, dtoHeader.getNome());
        FlatRecord recordNormal = new FakePositionalVariableRecord(Utility.rightPad(FakePositionalVariableRecord.NORMAL.getTypeString(), 37));
        recordNormal.setString(1, name);
        FakeNormalDTO dtoNormal = recordNormal.createDTO(FakePositionalVariableRecord.NORMAL);
        Assert.assertEquals(name, dtoNormal.getNome());
        FlatRecord record = new FakePositionalVariableRecord(Utility.rightPad(FakePositionalVariableRecord.FOOTER.getTypeString(), 17));
        record.setString(1, name);
        FakeFooterDTO dtoFooter = record.createDTO(FakePositionalVariableRecord.FOOTER);
        Assert.assertEquals(name, dtoFooter.getNome());
    }

    private static class FakePositionalVariableRecord extends AbstractPositionalVariableRecord {

        public static final RecordTypeDTO<FakeHeaderDTO> HEADER = new RecordTypeDTO<FakeHeaderDTO>(
                "01",
                new int[]{2,5,10},
                new FlatRecordDTOFactory<FakeHeaderDTO>() {
                    public FakeHeaderDTO createDTO(FlatRecord record) {
                        return new FakeHeaderDTO(record);
                    }
        });
        public static final RecordTypeDTO<FakeNormalDTO> NORMAL = new RecordTypeDTO<FakeNormalDTO>(
                "02",
                new int[]{2, 5, 10, 20},
                new FlatRecordDTOFactory<FakeNormalDTO>() {
                    public FakeNormalDTO createDTO(FlatRecord record) {
                        return new FakeNormalDTO(record);
                    }
        });
        public static final RecordTypeDTO<FakeFooterDTO> FOOTER = new RecordTypeDTO<FakeFooterDTO>(
                "03",
                new int[]{2, 5, 10},
                new FlatRecordDTOFactory<FakeFooterDTO>() {
                    public FakeFooterDTO createDTO(FlatRecord record) {
                        return new FakeFooterDTO(record);
                    }
        });

        private static final Map<String, RecordType> map = RecordType.buildMap(HEADER, NORMAL, FOOTER);

        public FakePositionalVariableRecord(String record) throws InvalidRecordException {
            super(map, record);
        }

        private FakePositionalVariableRecord(RecordType recordType, String str) throws InvalidRecordException {
            super(recordType, str);
        }

        public String extractType(String str) {
            return Utility.substring(str, 0, 2);
        }

    }

    private static class FakeHeaderDTO extends FlatRecordDTO {

        public FakeHeaderDTO(FlatRecord record) {
            super(record);
        }

        public String getNome() throws InvalidRecordException {
            return record.getString(1);
        }
    }

    private static class FakeNormalDTO extends FlatRecordDTO {

        public FakeNormalDTO(FlatRecord record) {
            super(record);
        }

        public String getNome() throws InvalidRecordException {
            return record.getString(1);
        }
    }

    private static class FakeFooterDTO extends FlatRecordDTO {

        public FakeFooterDTO(FlatRecord record) {
            super(record);
        }

        public String getNome() throws InvalidRecordException {
            return record.getString(1);
        }
    }
}
