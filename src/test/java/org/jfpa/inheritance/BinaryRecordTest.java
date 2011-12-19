package org.jfpa.inheritance;

import org.jfpa.exception.JfpaException;
import org.jfpa.interfaces.RecordHandler;
import org.jfpa.record.AbstractBinaryRecord;
import org.jfpa.builder.BinaryRecordBuilder;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.utility.Utility;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 13/04/11
 */
public class BinaryRecordTest {

    private static final String[] lines = {
            "{1:1234567",
            "89{1:12345",
            "6{1:ABC{1:A"
    };

    private int complete;

    @Before
    public void setUp() throws Exception {
        complete = 0;
    }

    @Test
    public void testBasic() throws Exception {
        String line = "{1:";
        FakeBinaryRecord record = new FakeBinaryRecord(line.getBytes());
        Assert.assertEquals(line, record.toString());
    }

    @Test
    public void testEmpty() throws Exception {
        Assert.assertFalse(new FakeBinaryRecord().isComplete());
    }

    @Test
    public void testEncoding() throws Exception {
        String line = "{1:";
        String cp500 = "Cp500";
        FakeBinaryRecord record = new FakeBinaryRecord(line.getBytes(cp500), cp500);
        Assert.assertTrue(record.isComplete());
        Assert.assertEquals(line, record.toString());
    }

    @Test(expected = JfpaException.class)
    public void testBadEncoding() throws Exception {
        String line = "{1:";
        String badOne = "BadOne";
        FakeBinaryRecord record = new FakeBinaryRecord(line.getBytes(), badOne);
        Assert.assertEquals(line, record.toString());
    }

    @Test
    public void testOk() throws Exception {
        BinaryRecordBuilder<FakeBinaryRecord> builder = new BinaryRecordBuilder<FakeBinaryRecord>(new FakeBinaryRecord(), new RecordHandler<FakeBinaryRecord>() {
            public void handle(FakeBinaryRecord record) {
                complete(record);
            }
        });
        Assert.assertTrue(builder.isEmpty());
        Assert.assertEquals(0, complete);
        for (String line : lines) {
            builder.process(line.getBytes());
        }
        Assert.assertFalse(builder.isEmpty());
        Assert.assertEquals(3, complete);
        builder.flush();
        Assert.assertEquals(4, complete);
    }

    @Test(expected = InvalidRecordException.class)
    public void testKO() throws Exception {
        BinaryRecordBuilder<TestBinaryRecord> builder = new BinaryRecordBuilder<TestBinaryRecord>(new TestBinaryRecord(), new RecordHandler<TestBinaryRecord>() {
            public void handle(TestBinaryRecord record) {
                complete(record);
            }
        });
        for (String line : lines) {
            builder.process(line.getBytes());
        }
        builder.flush();
    }

    @Test
    public void testOutOfSync() throws Exception {
        BinaryRecordBuilder<FakeBinaryRecord> builder = new BinaryRecordBuilder<FakeBinaryRecord>(new FakeBinaryRecord(), new RecordHandler<FakeBinaryRecord>() {
            public void handle(FakeBinaryRecord record) {
                complete(record);
            }
        });
        for (int i=1; i<lines.length; i++) {
            try {
                builder.process(lines[i].getBytes());
            } catch (InvalidRecordException e) {
                Assert.assertTrue(e.getMessage().startsWith("Out of sync"));
            }
        }
        builder.flush();
        Assert.assertEquals(3, complete);
    }

    public void complete(FakeBinaryRecord record) {
        Assert.assertNotNull(record);
        Assert.assertTrue(record.isComplete());
        complete++;
    }

    public static class FakeBinaryRecord extends AbstractBinaryRecord {

        public static final byte[] PATTERN = "{1:".getBytes();

        public FakeBinaryRecord() {}

        public FakeBinaryRecord(byte[] bytes) throws InvalidRecordException {
            super(bytes);
        }

        public FakeBinaryRecord(byte[] bytes, String encoding) throws InvalidRecordException {
            super(bytes, encoding);
        }

        @Override
        public byte[] getPattern() {
            return PATTERN;
        }
    }

    public static class TestBinaryRecord extends FakeBinaryRecord {
        @Override
        public boolean isComplete() {
            return bytes.length > 4;
        }
    }

    @Test
    public void testBig() throws Exception {
        BinaryRecordBuilder<FakeBinaryRecord> builder = new BinaryRecordBuilder<FakeBinaryRecord>(new FakeBinaryRecord(), new RecordHandler<FakeBinaryRecord>() {
            public void handle(FakeBinaryRecord record) {
                complete(record);
            }
        });
        builder.process(Utility.rightPad("{1:ABC", 300).getBytes());
        builder.flush();
        builder.flush();
    }

    public static class FakeBinary extends AbstractBinaryRecord {

        public static final byte[] PATTERN = "111:".getBytes();

        @Override
        public byte[] getPattern() {
            return PATTERN;
        }
    }

    @Test
    public void testPattern() throws Exception {
        BinaryRecordBuilder<FakeBinary> builder = new BinaryRecordBuilder<FakeBinary>(new FakeBinary(), new RecordHandler<FakeBinary>() {
            public void handle(FakeBinary record) throws InvalidRecordException {
            }
        });
        try {
            builder.process("1121:222111:333".getBytes());
        } catch (InvalidRecordException ignore) {}
    }
}
