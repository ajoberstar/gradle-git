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

import org.ajoberstar.grgit.Grgit

import org.junit.Rule
import org.junit.rules.TemporaryFolder

import spock.lang.Specification
import spock.lang.Unroll

class NearestVersionLocatorSpec extends Specification {
	@Rule TemporaryFolder tempDir = new TemporaryFolder()

	Grgit grgit

	def setup() {
		File repoDir = tempDir.newFolder('repo')
		grgit = Grgit.init(dir: repoDir)

		commit()
		commit()
		grgit.branch.add(name: 'unreachable')

		commit()
		grgit.tag.add(name: '0.0.1-beta.3')
		grgit.branch.add(name: 'no-normal')

		commit()
		grgit.tag.add(name: '0.1.0')

		commit()
		grgit.branch.add(name: 'RB_0.1')

		commit()
		commit()
		grgit.tag.add(name: '0.2.0')

		grgit.checkout(branch: 'RB_0.1')

		commit()
		grgit.tag.add(name: 'v0.1.1+2010.01.01.12.00.00')

		commit()
		commit()
		commit()
		commit()
		grgit.tag.add(name: 'v0.1.2-beta.1')

		commit()
		commit()
		commit()
		grgit.checkout(branch: 'master')

		commit()
		grgit.tag.add(name: 'v1.0.0')
		grgit.branch.add(name: 'RB_1.0')

		commit()
		grgit.tag.add(name: '1.1.0-rc.1+abcde')
	}

	@Unroll('when on #head, locator finds normal #normal with distance #distance and nearest #any at #stage')
	def 'locator returns correct value'() {
		given:
		grgit.checkout(branch: head)
		expect:
		def nearest = NearestVersionLocator.locate(grgit)
		nearest.any == Version.valueOf(any)
		nearest.normal == Version.valueOf(normal)
		nearest.distance == distance
		nearest.stage == stage
		where:
		head          | any                | normal                      | distance | stage
		'master'      | '1.1.0-rc.1+abcde' | '1.0.0'                     | 1        | 'rc'
		'RB_0.1'      | '0.1.2-beta.1'     | '0.1.1+2010.01.01.12.00.00' | 7        | 'beta'
		'RB_1.0'      | '1.0.0'            | '1.0.0'                     | 0        | ''
		'no-normal'   | '0.0.1-beta.3'     | '0.0.0'                     | 3        | 'beta'
		'unreachable' | '0.0.0'            | '0.0.0'                     | 2        | ''
	}

	private void commit() {
		new File(grgit.repository.rootDir, '1.txt') << '1'
		grgit.add(patterns: ['1.txt'])
		grgit.commit(message: 'do')
	}
}
