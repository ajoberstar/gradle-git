/*
 * Copyright 2012-2013 the original author or authors.
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
package org.ajoberstar.grgit.util

import spock.lang.Specification

class ConfigureUtilSpec extends Specification {
	private class Fake {
		private _element
		String name
		List items
		void setElement(String element) {
			this._element = element
		}
	}

	def 'configure with map works for valid setters'() {
		given:
		def map = [name:'Test', items:['one', 'two'], element:'Test2']
		def object = new Fake()
		when:
		ConfigureUtil.configure(object, map)
		then:
		object.name == 'Test'
		object.items == ['one', 'two']
		object._element == 'Test2'
	}

	def 'configure with map fails for invalid setter'() {
		when:
		ConfigureUtil.configure(new Fake(), [name:'Test', other:'Should fail'])
		then:
		thrown(MissingFieldException)
	}

	def 'configure with closre works for valid methods'() {
		given:
		def object = new Fake()
		when:
		ConfigureUtil.configure(object) {
			name = 'Test'
			items = ['one', 'two']
			setElement('Test2')
		}
		then:
		object.name == 'Test'
		object.items == ['one', 'two']
		object._element == 'Test2'		
	}

	def 'configure with closure fails for invalid method'() {
		when:
		ConfigureUtil.configure(new Fake()) {
			unrealMethod 'Should fail'
		}
		then:
		thrown(MissingMethodException)
	}
}
