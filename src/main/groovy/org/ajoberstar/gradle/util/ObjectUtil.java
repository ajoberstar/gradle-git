/* Copyright 2012 the original author or authors.
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
 * 
 * @since 0.1.0
 */
public class ObjectUtil {
	private ObjectUtil() {
		throw new AssertionError("Cannot instantiate this class");
	}
	
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
	
	public static String unpackString(Object obj) {
		Object value = unpack(obj);
		return value == null ? null : value.toString();
	}
}
