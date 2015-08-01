/*
 * Copyright 2012-2015 the original author or authors.
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

class NearestVersionLocatorSpec extends Specification {
	@Shared File repoDir

	@Shared Grgit grgit

	@Shared SecureRandom random = new SecureRandom()

	def setupSpec() {
		repoDir = Files.createTempDirectory('repo').toFile()
		grgit = Grgit.init(dir: repoDir)

		commit()
		commit()
		addBranch('unreachable')

		commit()
		addTag('0.0.1-beta.3')
		addBranch('no-normal')

		commit()
		addTag('0.1.0')

		commit()
		addBranch('RB_0.1')

		commit()
		commit()
		addTag('0.2.0')

		checkout('RB_0.1')

		commit()
		addTag('v0.1.1+2010.01.01.12.00.00')

		commit()
		commit()
		commit()
		commit()
		addTag('v0.1.2-beta.1')
		addTag('v0.1.2-alpha.1')

		commit()
		commit()
		commit()
		checkout('master')

		commit()
		addTag('v1.0.0')
		addTag('v1.0.0-rc.3')
		addBranch('RB_1.0')

		commit()
		addTag('1.1.0-rc.1+abcde')
	}

	def cleanupSpec() {
		assert !repoDir.exists() || repoDir.deleteDir()
	}

	@Unroll('when on #head, locator finds normal #normal with distance #distance and nearest #any at #stage')
	def 'locator returns correct value'() {
		given:
		grgit.checkout(branch: head)
		expect:
		def nearest = new NearestVersionLocator().locate(grgit)
		nearest.any == Version.valueOf(any)
		nearest.normal == Version.valueOf(normal)
		nearest.distanceFromNormal == distance
		where:
		head          | any                | normal                      | distance
		'master'      | '1.1.0-rc.1+abcde' | '1.0.0'                     | 1
		'RB_0.1'      | '0.1.2-beta.1'     | '0.1.1+2010.01.01.12.00.00' | 7
		'RB_1.0'      | '1.0.0'            | '1.0.0'                     | 0
		'no-normal'   | '0.0.1-beta.3'     | '0.0.0'                     | 3
		'unreachable' | '0.0.0'            | '0.0.0'                     | 2
	}

	@Unroll('when on #head, locator with parallel finds normal #normal with distance #distance and nearest #any at #stage')
	def 'locator with parallel returns correct value'() {
		given:
		grgit.checkout(branch: head)
		expect:
		def nearest = new NearestVersionLocator().locate(grgit, true)
		nearest.any == Version.valueOf(any)
		nearest.normal == Version.valueOf(normal)
		nearest.distanceFromNormal == distance
		where:
		head          | any                | normal                      | distance
		'master'      | '1.1.0-rc.1+abcde' | '1.0.0'                     | 1
		'RB_0.1'      | '0.1.2-beta.1'     | '0.1.1+2010.01.01.12.00.00' | 7
		'RB_1.0'      | '1.0.0'            | '1.0.0'                     | 0
		'no-normal'   | '0.0.1-beta.3'     | '0.0.0'                     | 3
		'unreachable' | '0.0.0'            | '0.0.0'                     | 2
	}

	private void commit() {
		byte[] bytes = new byte[128]
		random.nextBytes(bytes)
		new File(grgit.repository.rootDir, '1.txt') << bytes
		grgit.add(patterns: ['1.txt'])
		def commit = grgit.commit(message: 'do')
		println "Created commit: ${commit.abbreviatedId}"
	}

	private void addBranch(String name) {
		def currentHead = grgit.head()
		def currentBranch = grgit.branch.current
		def newBranch = grgit.branch.add(name: name)
		def atCommit = grgit.resolve.toCommit(newBranch.fullName)
		println "Added new branch ${name} at ${atCommit.abbreviatedId}"
		assert currentBranch == grgit.branch.current
		assert currentHead == atCommit
	}

	private void addTag(String name) {
		def currentHead = grgit.head()
		def newTag = grgit.tag.add(name: name)
		def atCommit = grgit.resolve.toCommit(newTag.fullName)
		println "Added new tag ${name} at ${atCommit.abbreviatedId}"
		assert currentHead == atCommit
	}

	private void checkout(String name) {
		def currentHead = grgit.head()
		grgit.checkout(branch: name)
		def atCommit = grgit.resolve.toCommit(name)
		def newHead = grgit.head()
		println "Checkout out ${name}, which is at ${atCommit.abbreviatedId}"
		assert currentHead != grgit.head()
		assert atCommit == newHead
		assert name == grgit.branch.current.name
	}
}
