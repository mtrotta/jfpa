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

import org.jfpa.interfaces.TypeExtractor;
import org.jfpa.manager.NullExtractor;
import org.jfpa.utility.Utility;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a record which can have a variable number
 * of columns or is made by multiple lines.
 * A multiple record consists of one or more{@link SubRecord},
 * which must be a class annotated with {@link Delimited}
 */
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface MultipleDelimited {

    /**
     * Delimiter string used only to extract record type
     */
    String delimiter() default Utility.EMPTY_STRING;

    /**
     * Integer position of column that contains record type
     */
    int typePosition() default -1;

    /**
     * For more complex type extraction you can specify a
     * {@link TypeExtractor} that returns a String containing
     * record type
     */
    Class<? extends TypeExtractor> typeExtractor() default NullExtractor.class;
}
