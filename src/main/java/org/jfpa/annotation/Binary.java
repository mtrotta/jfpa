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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Identifies a string record read by processing a binary stream.
 */
@Target(value = ElementType.TYPE)
@Retention(value = RetentionPolicy.RUNTIME)
public @interface Binary {
    /**
     * Byte pattern used to identify the beginning of a new record.
     */
    byte[] pattern() default { };

    /**
     * String pattern used to identify the beginning of a new record.
     * patternString will be automatically converted to byte pattern,
     * width default encoding or with encoding specified by
     * <code>encoding</code> param.
     */
    String patternString() default "";

    /**
     * String encoding name used to convert the binary stream to String.
     */
    String encoding() default "";
}
