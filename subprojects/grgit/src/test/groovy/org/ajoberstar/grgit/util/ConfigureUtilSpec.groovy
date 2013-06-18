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
