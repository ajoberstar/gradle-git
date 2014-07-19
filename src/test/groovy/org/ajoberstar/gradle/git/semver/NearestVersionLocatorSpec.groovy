/*
 * Copyright 2012-2014 the original author or authors.
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
package org.ajoberstar.gradle.git.semver

import com.github.zafarkhaja.semver.Version
import spock.lang.Unroll

class NearestVersionLocatorSpec extends GrgitSpec {
	def setupSpec() {
		commit()
		commit()
		// grgit.branch.add(name: 'unreachable')
		addBranch('unreachable')

		commit()
		// grgit.tag.add(name: '0.0.1-beta.3')
		addTag('0.0.1-beta.3')
		// grgit.branch.add(name: 'no-normal')
		addBranch('no-normal')

		commit()
		// grgit.tag.add(name: '0.1.0')
		addTag('0.1.0')

		commit()
		// grgit.branch.add(name: 'RB_0.1')
		addBranch('RB_0.1')

		commit()
		commit()
		// grgit.tag.add(name: '0.2.0')
		addTag('0.2.0')

		// grgit.checkout(branch: 'RB_0.1')
		checkout('RB_0.1')

		commit()
		// grgit.tag.add(name: 'v0.1.1+2010.01.01.12.00.00')
		addTag('v0.1.1+2010.01.01.12.00.00')

		commit()
		commit()
		commit()
		commit()
		// grgit.tag.add(name: 'v0.1.2-beta.1')
		addTag('v0.1.2-beta.1')
		addTag('v0.1.2-alpha.1')

		commit()
		commit()
		commit()
		// grgit.checkout(branch: 'master')
		checkout('master')

		commit()
		// grgit.tag.add(name: 'v1.0.0')
		addTag('v1.0.0')
		addTag('v1.0.0-rc.3')
		// grgit.branch.add(name: 'RB_1.0')
		addBranch('RB_1.0')

		commit()
		// grgit.tag.add(name: '1.1.0-rc.1+abcde')
		addTag('1.1.0-rc.1+abcde')
	}

	@Unroll('when on #head, locator finds normal #normal with distance #distance and nearest #any at #stage')
	def 'locator returns correct value'() {
		given:
		grgit.checkout(branch: head)
		expect:
		def nearest = NearestVersionLocator.locate(grgit)
		nearest.any == Version.valueOf(any)
		nearest.normal == Version.valueOf(normal)
		nearest.distanceFromNormal == distance
		nearest.stage == stage
		where:
		head          | any                | normal                      | distance | stage
		'master'      | '1.1.0-rc.1+abcde' | '1.0.0'                     | 1        | 'rc'
		'RB_0.1'      | '0.1.2-beta.1'     | '0.1.1+2010.01.01.12.00.00' | 7        | 'beta'
		'RB_1.0'      | '1.0.0'            | '1.0.0'                     | 0        | ''
		'no-normal'   | '0.0.1-beta.3'     | '0.0.0'                     | 3        | 'beta'
		'unreachable' | '0.0.0'            | '0.0.0'                     | 2        | ''
	}

	private void addBranch(String name) {
		def currentHead = grgit.head()
		def currentBranch = grgit.branch.current
		def newBranch = grgit.branch.add(name: name)
		def atCommit = grgit.resolveCommit(newBranch.fullName)
		println "Added new branch ${name} at ${atCommit.abbreviatedId}"
		assert currentBranch == grgit.branch.current
		assert currentHead == atCommit
	}

	private void addTag(String name) {
		def currentHead = grgit.head()
		def newTag = grgit.tag.add(name: name)
		def atCommit = grgit.resolveCommit(newTag.fullName)
		println "Added new tag ${name} at ${atCommit.abbreviatedId}"
		assert currentHead == atCommit
	}

	private void checkout(String name) {
		def currentHead = grgit.head()
		grgit.checkout(branch: name)
		def atCommit = grgit.resolveCommit(name)
		def newHead = grgit.head()
		println "Checkout out ${name}, which is at ${atCommit.abbreviatedId}"
		assert currentHead != grgit.head()
		assert atCommit == newHead
		assert name == grgit.branch.current.name
	}
}
