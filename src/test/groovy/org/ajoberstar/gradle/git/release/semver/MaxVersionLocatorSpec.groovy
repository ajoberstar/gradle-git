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
package org.ajoberstar.gradle.git.release.semver

import java.nio.file.Files
import java.security.SecureRandom

import com.github.zafarkhaja.semver.Version

import org.ajoberstar.grgit.Grgit

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class MaxVersionLocatorSpec extends VersionLocatorSpecBase {

	def setupSpec() {
		repoDir = Files.createTempDirectory('repo').toFile()
		grgit = Grgit.init(dir: repoDir)

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

	def cleanupSpec() {
		assert !repoDir.exists() || repoDir.deleteDir()
	}

	@Unroll('when on #head, locator finds normal #normal and nearest #any')
	def 'locator returns correct value'() {
		given:
		grgit.checkout(branch: head)
		expect:
		def max = new MaxVersionLocator().locate(grgit)
		max.any == Version.valueOf(any)
		max.normal == Version.valueOf(normal)
		where:
		head          | any                | normal
		'master'      | '1.1.0-rc.1+abcde' | '1.0.0'
	}
}