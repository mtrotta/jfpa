package org.jfpa.manager;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 02/10/11
 */
public enum ColumnType {

    STRING,
    DATE,
    INTEGER,
    LONG,
    DOUBLE,
    BIG_DECIMAL,
    BOOLEAN,
    CUSTOM;

    private static final Map<Class, ColumnType> VALID_TYPES = new HashMap<Class, ColumnType>() { {
        put(String.class, ColumnType.STRING);
        put(Date.class, ColumnType.DATE);
        put(Integer.class, ColumnType.INTEGER);
        put(Long.class, ColumnType.LONG);
        put(Double.class, ColumnType.DOUBLE);
        put(BigDecimal.class, ColumnType.BIG_DECIMAL);
        put(Boolean.class, ColumnType.BOOLEAN);
    } };

    public static ColumnType valueOf(final Class clazz) {
        return VALID_TYPES.get(clazz);
    }

    public static Set<Class> getValidTypes() {
        return VALID_TYPES.keySet();
    }
}
