/*
 * Copyright (c) 2011 Matteo Trotta
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jfpa.annotation;

import org.jfpa.utility.Utility;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * It's used to specify a mapped column for a file column, both positional or
 * delimited.
 */
@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface TextColumn {

    /**
     * Length of column.
     * For @Positional is the fixed length of the column, and it's mandatory
     * For @Delimited is the maximum length of the column, if not required
     * a zero or negative value can be used for unlimited length
     */
    int length();

    /**
     * Description of column.
     * Used to compose header, if requested
     */
    String description() default "";

    /**
     * Specifies the offset for column from previous column, or beginning (if first).
     * For @Positional the offset is expressed in chars
     * For @Delimited the offset is expressed in columns.
     * If not specified default value is zero.
     */
    int offset() default 0;

    /**
     * Specifies the string format for Date columns (example: "dd/MM/yyyy")
     * If not specified, the default date format is used, for more info
     * see {@link org.jfpa.manager.RecordManager#getDefaultDateFormat() defaultDateFormat}
     * If it's specified on non Date column, param is ignored
     */
    String dateFormat() default Utility.EMPTY_STRING;

    /**
     * Specifies the format for Boolean.
     * Since boolean is a true/false value, a String array with 2 elements is
     * expected. The first value should be the String that corresponds to TRUE,
     * while the second value should be the String that corresponds to FALSE.
     * If not specified, the default boolean format is used, for more info
     * see {@link org.jfpa.manager.RecordManager#getDefaultBooleanFormat() defaultBooleanFormat}
     * If it's specified on non Boolean columns, param is ignored
     */
    String[] booleanFormat() default { };

    /**
     * Specifies whether to invalidate record in case of bad value conversion.
     * For example if a Date conversion fails because of a bad format, a <code>true</code>
     * value of <code>invalidateOnError</code> will produce a <code>InvalidRecordException</code>.
     * Instead a <code>false</code> value of <code>invalidateOnError</code> will ignore
     * the value for this column, and will leave a null value, without throwing a
     * @see org.jfpa.exception.InvalidRecordException
     */
    boolean invalidateOnError() default true;
}
