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

class NearestVersionTagLocatorSpec extends Specification {
	@Rule TemporaryFolder tempDir = new TemporaryFolder()

	Grgit grgit
	List commits = []
	int commitNum = 0

	def setup() {
		File repoDir = tempDir.newFolder('repo')
		grgit = Grgit.init(dir: repoDir)

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
		grgit.tag.add(name: 'v0.1.2-beta.1')


		grgit.checkout(branch: 'master')

		commit()
		grgit.tag.add(name: 'v1.0.0')
		grgit.branch.add(name: 'RB_1.0')

		commit()
		grgit.tag.add(name: '1.1.0-rc.1+abcde')
	}

	@Unroll('#locator from #revstr returns #version')
	def 'locator returns correct version'() {
		expect:
		locator.newInstance().locate(grgit, revstr) == version
		where:
		locator                        | revstr   | version
		NearestVersionTagLocator       | 'HEAD'   | Version.valueOf('1.1.0-rc.1+abcde')
		NearestNormalVersionTagLocator | 'HEAD'   | Version.valueOf('1.0.0')
		NearestVersionTagLocator       | 'RB_0.1' | Version.valueOf('0.1.2-beta.1')
		NearestNormalVersionTagLocator | 'RB_0.1' | Version.valueOf('0.1.1+2010.01.01.12.00.00')
	}

	private void commit() {
		new File(grgit.repository.rootDir, '1.txt') << '1'
		grgit.add(patterns: ['1.txt'])
		commits << grgit.commit(message: "do ${++commitNum}")
	}
}
