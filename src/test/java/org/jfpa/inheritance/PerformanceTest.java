package org.jfpa.inheritance;

import org.jfpa.dto.FlatRecordDTO;
import org.jfpa.exception.InvalidRecordException;
import org.jfpa.interfaces.FlatRecord;
import org.jfpa.interfaces.FlatRecordDTOFactory;
import org.jfpa.record.PositionalRecord;
import org.jfpa.type.RecordTypeDTO;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 30/03/11
 */
public class PerformanceTest {

    private final int cycles = 100000;
    private final String line = "1    2    1234567890";

    @Ignore
    @Test
    public void test() throws Exception {
        long normal = testNormal();
        long reflection = testReflection();
        long reflectionCache = testReflectionCache();
        Assert.assertTrue(normal < reflection);
        Assert.assertTrue(normal < reflectionCache);
        Assert.assertTrue(reflectionCache < reflection);
    }

    public long testNormal() throws Exception {
        long begin = System.currentTimeMillis();
        for (int i=0; i<cycles; i++) {
            MyRecord record = new MyRecord(line);
            FakeDTO dto = record.createDTO(MyRecord.DEFAULT);
            Assert.assertNotNull(dto);
        }
        return System.currentTimeMillis() - begin;
    }

    public long testReflection() throws Exception {
        long begin = System.currentTimeMillis();
        for (int i=0; i<cycles; i++) {
            ReflectionRecord record = new ReflectionRecord(line);
            FakeDTO dto = record.createDTO(FakeDTO.class);
            Assert.assertNotNull(dto);
        }
        return System.currentTimeMillis() - begin;
    }

    public long testReflectionCache() throws Exception {
        long begin = System.currentTimeMillis();
        for (int i=0; i<cycles; i++) {
            CacheRecord record = new CacheRecord(line);
            FakeDTO dto = record.createDTO(FakeDTO.class);
            Assert.assertNotNull(dto);
        }
        return System.currentTimeMillis() - begin;
    }

    private static class MyRecord extends PositionalRecord {

        private static final int[] LENGTHS = new int[]{5, 5, 10};

        private static final FlatRecordDTOFactory<FakeDTO> factory = new FlatRecordDTOFactory<FakeDTO>() {
            public FakeDTO createDTO(FlatRecord record) {
                return new FakeDTO(record);
            }
        };

        public static final RecordTypeDTO<FakeDTO> DEFAULT = new RecordTypeDTO<FakeDTO>(LENGTHS, factory);

        public MyRecord(String str) throws InvalidRecordException {
            super(DEFAULT, str);
        }
    }

    private static class FakeDTO extends FlatRecordDTO {

        public FakeDTO(FlatRecord record) {
            super(record);
        }
    }

    private static class ReflectionRecord extends PositionalRecord {

        private static final int[] LENGTHS = new int[]{5, 5, 10};

        private static final FlatRecordDTOFactory<FakeDTO> factory = new FlatRecordDTOFactory<FakeDTO>() {
            public FakeDTO createDTO(FlatRecord record) {
                return new FakeDTO(record);
            }
        };

        public static final RecordTypeDTO<FakeDTO> DEFAULT = new RecordTypeDTO<FakeDTO>(LENGTHS, factory);

        public ReflectionRecord(String str) throws InvalidRecordException {
            super(DEFAULT, str);
        }

        public <T extends FlatRecordDTO> T createDTO(Class<T> clazz) {
            try {
                Constructor<T> constructor = clazz.getConstructor(FlatRecord.class);
                return constructor.newInstance(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private static class CacheRecord extends PositionalRecord {

        private static final int[] LENGTHS = new int[]{5, 5, 10};

        private static final FlatRecordDTOFactory<FakeDTO> factory = new FlatRecordDTOFactory<FakeDTO>() {
            public FakeDTO createDTO(FlatRecord record) {
                return new FakeDTO(record);
            }
        };

        public static final RecordTypeDTO<FakeDTO> DEFAULT = new RecordTypeDTO<FakeDTO>(LENGTHS, factory);

        private static Map<Class<?>, Constructor<?>> map = fillMap(FakeDTO.class);

        public CacheRecord(String str) throws InvalidRecordException {
            super(DEFAULT, str);
        }

        @SuppressWarnings(value = "unchecked")
        public <T extends FlatRecordDTO> T createDTO(Class<T> clazz) {
            try {
                Constructor<T> constructor = (Constructor<T>) map.get(clazz);
                return constructor.newInstance(this);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    @SuppressWarnings(value = "unchecked")
    private static Map<Class<?>, Constructor<?>> fillMap(Class... classes) {
        Map<Class<?>, Constructor<?>> map = new HashMap<Class<?>, Constructor<?>>();
        try {
            for (Class clazz : classes) {
                Constructor constructor = clazz.getConstructor(FlatRecord.class);
                map.put(clazz, constructor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

}
