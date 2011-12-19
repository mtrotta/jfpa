package org.jfpa.inheritance;

import org.jfpa.exception.InvalidRecordException;
import org.jfpa.interfaces.FlatRecord;
import org.jfpa.interfaces.FlatRecordDTOFactory;
import org.jfpa.record.*;
import org.jfpa.dto.FlatRecordDTO;
import org.jfpa.type.RecordType;
import org.jfpa.type.RecordTypeDTO;
import org.jfpa.utility.Utility;
import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 23/03/11
 */
public class DelimitedVariableRecordTest {

    @Test
    public void testDelimited() throws Exception  {
        FlatRecord record = new FakeAbstractDelimitedVariableRecord("AAA:ZZZ");
        Assert.assertEquals("AAA", record.getType().getTypeString());
        Assert.assertTrue(record.isType(FakeAbstractDelimitedVariableRecord.DEFAULT));
        record.validate();
    }

    @Test
    public void testBasic() throws Exception {
        String line = "AAA:";
        FakeAbstractDelimitedVariableRecord record = new FakeAbstractDelimitedVariableRecord(FakeAbstractDelimitedVariableRecord.DEFAULT, line);
        Assert.assertEquals(line, record.toString());
    }

    @Test(expected = InvalidRecordException.class)
    public void testBad() throws Exception {
        String line = "ZZZ:";
        new FakeAbstractDelimitedVariableRecord(line);
    }

    @Test
    public void testDto() throws Exception  {
        FlatRecord record = new FakeAbstractDelimitedVariableRecord("AAA:Nome");
        FakeDTO dto = record.createDTO(FakeAbstractDelimitedVariableRecord.DEFAULT);
        Assert.assertEquals("Nome", dto.getNome());
    }

    private static class FakeAbstractDelimitedVariableRecord extends AbstractDelimitedVariableRecord {

        private static final String DELIMITER = ":";
        private static final String HEADER = "AAA";

        private static final FlatRecordDTOFactory<FakeDTO> factory = new FlatRecordDTOFactory<FakeDTO>() {
            public FakeDTO createDTO(FlatRecord record) {
                return new FakeDTO(record);
            }
        };

        public static final RecordTypeDTO<FakeDTO> DEFAULT = new RecordTypeDTO<FakeDTO>(HEADER, DELIMITER, 2, factory);

        private static final Map<String, RecordType> map = RecordType.buildMap(DEFAULT);

        public FakeAbstractDelimitedVariableRecord(String str) throws InvalidRecordException {
            super(map, str);
        }

        private FakeAbstractDelimitedVariableRecord(RecordType recordType, String str) throws InvalidRecordException {
            super(recordType, str);
        }

        public String extractType(String str) {
            return Utility.substring(str, 0, 3);
        }
    }

    private static class FakeDTO extends FlatRecordDTO {

        public FakeDTO(FlatRecord record) {
            super(record);
        }

        public String getNome() throws InvalidRecordException {
            return record.getString(1);
        }
    }
}
