package org.jfpa.utility;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class Utility {

    public static final String EMPTY_STRING = "";
    public static final String NEW_LINE = System.getProperty("line.separator");

    private Utility() { }

    public static boolean isEmpty(final String value) {
        return value == null || EMPTY_STRING.equals(value.trim());
    }

    public static String substring(final String string, final int start, final int end) {
        return substring(string, start, end, false);
    }

    public static String substring(final String string, final int start, int end, final boolean trim) {
        if (!isEmpty(string) && start < string.length()) {
            if (end > string.length()) {
                end = string.length();
            }
            return (trim ? string.substring(start, end).trim() : string.substring(start, end));
        }
        return null;
    }

    public static String buildString(String... strings) {
        StringBuilder builder = new StringBuilder();
        for (String string : strings) {
            if (string != null) {
                builder.append(string);
            }
        }
        return builder.toString();
    }

    public static String encloseString(String string, String stringEnclose) {
        return buildString(stringEnclose, string, stringEnclose);
    }

    public static String unencloseString(String string, String stringEnclose) {
        if (string != null) {
            int begin = string.indexOf(stringEnclose);
            int end = string.lastIndexOf(stringEnclose);
            if (begin < 0 || begin == end) {
                throw new IllegalArgumentException("string not properly enclosed: " + string);
            }
            return string.substring(begin + stringEnclose.length(), end);
        }
        return null;
    }

    public static String buildDelimitedString(final String delimiter, final Object[] objects) {
        StringBuilder builder = new StringBuilder();
        if (objects != null && objects.length > 0) {
            if (objects[0] != null) {
                builder.append(objects[0]);
            }
            for (int i = 1; i < objects.length; i++) {
                builder.append(delimiter);
                if (objects[i] != null) {
                    builder.append(objects[i]);
                }
            }
        }
        return builder.toString();
    }

    public static String buildNewLineString(final Object... values) {
        return buildNewLineString(Arrays.asList(values));
    }

    public static <T> String buildNewLineString(final Collection<T> collection) {
        StringBuilder builder = new StringBuilder();
        Iterator<T> iterator = collection.iterator();
        while (iterator.hasNext()) {
            T next =  iterator.next();
            if (next != null) {
                builder.append(next.toString());
            }
            if (iterator.hasNext()) {
                builder.append(NEW_LINE);
            }
        }
        return builder.toString();
    }

    public static <T> String buildNewLineStringFromList(final Collection<List<T>> collection) {
        StringBuilder builder = new StringBuilder();
        Iterator<List<T>> iterator = collection.iterator();
        while (iterator.hasNext()) {
            List<T> next =  iterator.next();
            if (next != null) {
                builder.append(buildNewLineString(next));
            }
            if (iterator.hasNext()) {
                builder.append(NEW_LINE);
            }
        }
        return builder.toString();
    }

    public static String trimString(String value) {
        if (value != null) {
            value = value.trim();
            return EMPTY_STRING.equals(value) ? null : value;
        }
        return null;
    }

    public static String spaces(final int length) {
        char[] spaces = new char[length];
        Arrays.fill(spaces, ' ');
        return new String(spaces);
    }

    public static String rightPad(final String str, final int length) {
        return String.format("%1$-" + length + "s", getValidString(str));
    }

    public static String getValidString(final String value) {
        return value == null ? EMPTY_STRING : value;
    }

    public static Date stringToDate(final String date, final String format) {
        if (!isEmpty(date)) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(format);
                sdf.setLenient(false);
                return sdf.parse(date.trim());
             } catch (ParseException e) {
                throw new IllegalArgumentException(e);
             }
        }
        return null;
    }

    public static String dateToString(final Date date, final String format) {
        return date != null ? new SimpleDateFormat(format).format(date) : null;
    }

    public static Integer stringToInteger(String value) {
        if (!isEmpty(value)) {
            try {
                if (value.startsWith("+")) {
                    value = value.substring(1);
                }
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return null;
    }

    public static String integerToString(final Integer value) {
        return value != null ? value.toString() : null;
    }

    public static String longToString(final Long value) {
        return value != null ? value.toString() : null;
    }

    public static Long stringToLong(String value) {
        if (!isEmpty(value)) {
            try {
                if (value.startsWith("+")) {
                    value = value.substring(1);
                }
                return Long.parseLong(value.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return null;
    }

    public static Double stringToDouble(String value) {
        if (!isEmpty(value)) {
            try {
                if (value.startsWith("+")) {
                    value = value.substring(1);
                }
                return Double.parseDouble(value.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return null;
    }

    public static String doubleToString(final Double value) {
        return value != null ? value.toString() : null;
    }

    public static BigDecimal stringToBigDecimal(final String bigDecimal) {
        if (!isEmpty(bigDecimal)) {
            try {
                String bigDecimalSep = bigDecimal.replace(',', '.');
                return new BigDecimal(bigDecimalSep.trim());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e);
            }
        }
        return null;
    }

    public static String bigDecimalToString(final BigDecimal bigDecimal) {
        return bigDecimal != null ? bigDecimal.toPlainString() : null;
    }

    public static String booleanToString(final Boolean value, final String[] trueFalse) {
        return value != null ? value ? trueFalse[0] : trueFalse[1] : null;
    }

    public static Boolean stringToBoolean(String value, final String[] trueFalse) {
        value = trimString(value);
        if (value != null) {
            if (value.equals(trueFalse[0])) {
                return true;
            }
            if (value.equals(trueFalse[1])) {
                return false;
            }
            throw new IllegalArgumentException("Value '" + value + "' is not in " + Arrays.toString(trueFalse));
        }
        return null;
    }

    public static String hexString(final byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b : bytes) {
            builder.append(String.format("0x%x ", b));
        }
        builder.append(String.format("(%d bytes) - ASCII: '%s'", bytes.length, new String(bytes)));
        return builder.toString();
    }

    public static <T> boolean checkDomain(final T value, final T...allowedValues) {
        if (value != null) {
            for (T allowed : allowedValues) {
                if (value.equals(allowed)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean containsAnyValues(final Object... values) {
        for (Object value : values) {
            if (value != null) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsAllValues(final Object... values) {
        for (Object value : values) {
            if (value == null) {
                return false;
            }
        }
        return true;
    }

    public static int[] convertArray(final List<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }
}
