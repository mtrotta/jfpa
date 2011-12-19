package org.jfpa.utility;

/**
 * Created by IntelliJ IDEA.
 * User: Matteo Trotta
 * Copyright © 2011 Matteo Trotta
 * Date: 02/10/11
 */
public class Formats {

    private Formats() { }

    public static final String DATE_FORMAT = "dd/MM/yyyy";
    public static final String DATE_FORMAT_INVERTED = "yyyy/MM/dd";
    public static final String DATE_FORMAT_DASH = "dd-MM-yyyy";
    public static final String DATE_FORMAT_DASH_INVERTED = "yyyy-MM-dd";
    public static final String DATE_FORMAT_DOT = "dd.MM.yyyy";
    public static final String DATE_FORMAT_DOT_INVERTED = "yyyy.MM.dd";
    public static final String DATE_FORMAT_CLEAN_INVERTED = "yyyyMMdd";
    public static final String DATE_FORMAT_CLEAN = "ddMMyyyy";
    public static final String DATE_FORMAT_TIMESTAMP = "yyyyMMddHHmmss";
    public static final String DATE_FORMAT_DATETIME = "dd/MM/yyyy HH:mm:ss.SSS";
    public static final String DATE_FORMAT_TIME = "HH.mm";
    public static final String DATE_FORMAT_TIME_SS = "HH:mm:ss";

    public static final String[] BOOLEAN_TRUE_FALSE = new String[] {"true", "false"};
    public static final String[] BOOLEAN_Y_N = new String[] {"Y", "N"};
}
