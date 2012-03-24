package org.jfpa.inheritance;

import org.jfpa.dto.FlatRecordDTO;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.interfaces.FlatRecordDTOFactory;
import org.jfpa.dto.MultipleFlatRecordDTO;
import org.jfpa.interfaces.MultipleFlatRecordDTOFactory;
import org.jfpa.interfaces.FlatRecord;
import org.jfpa.interfaces.MultipleFlatRecord;
import org.jfpa.record.*;
import org.jfpa.type.MultipleRecordTypeDTO;
import org.jfpa.type.RecordType;
import org.jfpa.type.RecordTypeDTO;
import org.jfpa.utility.Utility;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 24/03/11
 */
public class MultipleRecordTest {

    private FakePositionalVariableRecord header1;
    private FakePositionalVariableRecord normal1;
    private FakePositionalVariableRecord normal2_1;
    private FakePositionalVariableRecord footer;

    public MultipleRecordTest() throws InvalidRecordException {
        header1 = new FakePositionalVariableRecord("520Line1");
        normal1 = new FakePositionalVariableRecord("521Line2");
        normal2_1 = new FakePositionalVariableRecord("522Line2");
        footer = new FakePositionalVariableRecord("599Line2");
    }

    @Test
    public void test() throws Exception  {

        MultipleFlatRecord record = new FakeMultipleRecord();

        Assert.assertNull(record.getType());

        Assert.assertFalse(record.isComplete());
        Assert.assertTrue(record.isFirst(header1));
        record.addRecord(header1);
        Assert.assertTrue(record.isComplete());
        record.addRecord(normal1);
        Assert.assertTrue(record.isComplete());
        record.addRecord(normal2_1);
        Assert.assertEquals(3, record.size());

        Assert.assertTrue(record.containsAll(FakePositionalVariableRecord.HEADER_520, FakePositionalVariableRecord.NORMAL_521, FakePositionalVariableRecord.NORMAL_522));
        Assert.assertFalse(record.containsAll(FakePositionalVariableRecord.HEADER_520, FakePositionalVariableRecord.NORMAL_521,
                                                FakePositionalVariableRecord.NORMAL_522, FakePositionalVariableRecord.FOOTER_599));
        Assert.assertEquals(3, record.findRecords(FakePositionalVariableRecord.HEADER_520, FakePositionalVariableRecord.NORMAL_521, FakePositionalVariableRecord.NORMAL_522).size());
        Assert.assertEquals(3, record.getAllTypes().size());

        Assert.assertEquals(header1, record.findFirstRecord(FakePositionalVariableRecord.HEADER_520));
        Assert.assertNull(record.findFirstRecord(FakePositionalVariableRecord.FOOTER_599));

        Assert.assertNull(record.createFlatRecordDTO(FakePositionalVariableRecord.FOOTER_599));

        record.clear();
        Assert.assertTrue(record.isEmpty());
        Assert.assertFalse(record.isComplete());
        record.addRecord(normal1);
        Assert.assertTrue(record.isComplete());
        record.addRecord(normal2_1);
        record.addRecord(footer);
        Assert.assertEquals(3, record.size());

        List<FakeNormal521DTO> list521 = record.createFlatRecordDTOList(FakePositionalVariableRecord.NORMAL_521);
        Assert.assertEquals(1, list521.size());
        Assert.assertEquals(FakeNormal521DTO.class, list521.get(0).getClass());

        List<FakeNormal522DTO> list522 = record.createFlatRecordDTOList(FakePositionalVariableRecord.NORMAL_522);
        Assert.assertEquals(1, list522.size());
        Assert.assertEquals(FakeNormal522DTO.class, list522.get(0).getClass());

        FakeNormal522DTO dto522 = record.createFlatRecordDTO(FakePositionalVariableRecord.NORMAL_522);
        Assert.assertNotNull(dto522);
        Assert.assertEquals("Line2", dto522.getField522());
        dto522.setField522("Line");
        FlatRecord r = record.findFirstRecord(FakePositionalVariableRecord.NORMAL_522);
        Assert.assertEquals("Line", r.createDTO(FakePositionalVariableRecord.NORMAL_522).getField522());

        FakeMultipleDTO mDTO = record.createDTO(FakeMultipleRecord.DEFAULT);
        Assert.assertNotNull(mDTO);
        Assert.assertEquals("Line2", mDTO.getValue());

        FakeMultipleDTO mDTOT2 = record.createDTO(FakeMultipleRecord.T2);
        Assert.assertNotNull(mDTOT2);
    }

    private static class FakePositionalVariableRecord extends AbstractPositionalVariableRecord {

        private static final int[] LENGTHS = new int[]{3,5};

        public static final RecordType HEADER_520 = new RecordType("520", LENGTHS);

        public static final RecordTypeDTO<FakeNormal521DTO> NORMAL_521 = new RecordTypeDTO<FakeNormal521DTO>("521", LENGTHS, new FlatRecordDTOFactory<FakeNormal521DTO>() {
            public FakeNormal521DTO createDTO(FlatRecord record) {
                return new FakeNormal521DTO(record);
            }
        });

        public static final RecordTypeDTO<FakeNormal522DTO> NORMAL_522 = new RecordTypeDTO<FakeNormal522DTO>("522", LENGTHS, new FlatRecordDTOFactory<FakeNormal522DTO>() {
            public FakeNormal522DTO createDTO(FlatRecord record) {
                return new FakeNormal522DTO(record);
            }
        });
        public static final RecordTypeDTO<FakeNormal522DTO> FOOTER_599 = new RecordTypeDTO<FakeNormal522DTO>("599", LENGTHS, new FlatRecordDTOFactory<FakeNormal522DTO>() {
            public FakeNormal522DTO createDTO(FlatRecord record) {
                return new FakeNormal522DTO(record);
            }
        });

        private static final Map<String, RecordType> map = RecordType.buildMap(HEADER_520, NORMAL_521, NORMAL_522, FOOTER_599);

        public FakePositionalVariableRecord(String record) throws InvalidRecordException {
            super(map, record);
        }

        public String extractType(String str) {
            return Utility.substring(str, 0, 3);
        }
    }

    private static class FakeMultipleRecord extends AbstractMultipleRecord {

        public static final MultipleRecordTypeDTO<FakeMultipleDTO> DEFAULT = new MultipleRecordTypeDTO<FakeMultipleDTO>(new MultipleFlatRecordDTOFactory<FakeMultipleDTO>() {
            public FakeMultipleDTO createDTO(MultipleFlatRecord record) {
                return new FakeMultipleDTO(record);
            }
        });

        public static final MultipleRecordTypeDTO<FakeMultipleDTO> T2 = new MultipleRecordTypeDTO<FakeMultipleDTO>("T2", new MultipleFlatRecordDTOFactory<FakeMultipleDTO>() {
            public FakeMultipleDTO createDTO(MultipleFlatRecord record) {
                return new FakeMultipleDTO(record);
            }
        });

        public boolean isFirst(FlatRecord record) {
            return record.isType(FakePositionalVariableRecord.HEADER_520);
        }
    }

    private static class FakeNormal521DTO extends FlatRecordDTO {
        public FakeNormal521DTO(FlatRecord record) {
            super(record);
        }
    }

    private static class FakeNormal522DTO extends FlatRecordDTO {
        public FakeNormal522DTO(FlatRecord record) {
            super(record);
        }

        public String getField522() throws InvalidRecordException {
            return record.getString(1);
        }

        public void setField522(String s) throws InvalidRecordException {
            record.setString(1, s);
        }
    }

    private static class FakeMultipleDTO extends MultipleFlatRecordDTO {

        public FakeMultipleDTO(MultipleFlatRecord record) {
            super(record);
        }

        public String getValue() throws InvalidRecordException {
            return record.findFirstRecord(FakePositionalVariableRecord.NORMAL_521).getString(1);
        }

    }
}
