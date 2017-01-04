/*
 * Copyright 2012-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ajoberstar.gradle.util;

import java.util.concurrent.Callable;

import groovy.lang.Closure;

/**
 * Utility class for general {@code Object} related operations.
 * @since 0.1.0
 */
public final class ObjectUtil {
    /**
     * Cannot instantiate
     * @throws AssertionError always
     */
    private ObjectUtil() {
        throw new AssertionError("Cannot instantiate this class");
    }

    /**
     * Unpacks the given object by recursively
     * calling the {@code call()} method if the
     * object is a {@code Closure} or {@code Callable}.
     * @param obj the object to unpack
     * @return the unpacked value of the object
     */
    @SuppressWarnings("rawtypes")
    public static Object unpack(Object obj) {
        Object value = obj;
        while (value != null) {
            if (value instanceof Closure) {
                value = ((Closure) value).call();
            } else if (value instanceof Callable) {
                try {
                    value = ((Callable) value).call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            } else {
                return value;
            }
        }
        return value;
    }

    /**
     * Unpacks the given object to its {@code String}
     * value.  Same behavior as the other {@code unpack}
     * method ending with a call to {@code toString()}.
     * @param obj the value to unpack
     * @return the unpacked string value
     * @see ObjectUtil#unpack(Object)
     */
    public static String unpackString(Object obj) {
        Object value = unpack(obj);
        return value == null ? null : value.toString();
    }
}
