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

/**
 * Identifies a string record in which each column is separated
 * by a delimiter string, such as a semicolumn, pipe, etc.
 * Normally a single character is used as delimiter, but
 * any string can be used as delimiter for columns
 */
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Delimited {

    /**
     * Delimiter string, used as column separator
     */
    String delimiter();

    /**
     * Minimum column number that line should contain to be
     * considered a valid record. Parsing a line that contains
     * a smaller value of columns will throw a
     * {@link org.jfpa.exception.InvalidRecordException}
     */
    int minColumns() default 0;

    /**
     * String sequence used to enclose {@link org.jfpa.annotation.TextColumn}
     * of type String
     */
    String stringEnclose() default "";
}
