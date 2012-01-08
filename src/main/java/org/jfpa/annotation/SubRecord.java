/*
 * Copyright (c) 2012 Matteo Trotta
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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be used inside a {@link MultiplePositional}
 * or {@link MultipleDelimited} record. SubRecord should be used
 * on fields that are annotated with {@link Positional} or
 * {@link Delimited}
 */
@Target(value = ElementType.FIELD)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface SubRecord {

    /**
     * Type String used to map the record type while
     * parsing multiple record. After record type extraction
     * the corresponding SubRecord that match type String will
     * be used to fill record information.
     */
    String type();

    /**
     * 
     */
    boolean first() default false;
}
