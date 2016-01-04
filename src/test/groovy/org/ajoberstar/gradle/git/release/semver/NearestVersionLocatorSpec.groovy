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

import com.github.zafarkhaja.semver.Version
import org.ajoberstar.gradle.git.release.base.TagStrategy
import org.ajoberstar.grgit.Grgit
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files
import java.security.SecureRandom

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
		addTag('not-a-version')

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
		addTag('also-not-a-version')

		addBranch('test')
		checkout('test')
		commit()
		addTag('2.0.0-rc.1')
		addBranch('REL_2.1')
		commit()
		commit()
		checkout('REL_2.1')
		commit('2.txt')
		addTag('2.1.0-rc.1')
		checkout('test')
		merge('REL_2.1')
	}

	def cleanupSpec() {
		assert !repoDir.exists() || repoDir.deleteDir()
	}

	@Unroll('when on #head, locator finds normal #normal with nearest #any')
	def 'locator returns correct value'() {
		given:
		grgit.checkout(branch: head)
		expect:
		def nearest = new NearestVersionLocator(new TagStrategy()).locate(grgit)
		nearest.any == Version.valueOf(any)
		nearest.normal == Version.valueOf(normal)
		nearest.distanceFromAny == anyDistance
		nearest.distanceFromNormal == normalDistance
		where:
		head          | any                | normal                      | anyDistance | normalDistance
		'master'      | '1.1.0-rc.1+abcde' | '1.0.0'                     | 0           | 1
		'RB_0.1'      | '0.1.2-beta.1'     | '0.1.1+2010.01.01.12.00.00' | 3           | 7
		'RB_1.0'      | '1.0.0'            | '1.0.0'                     | 0           | 0
		'no-normal'   | '0.0.1-beta.3'     | '0.0.0'                     | 0           | 3
		'unreachable' | '0.0.0'            | '0.0.0'                     | 2           | 2
		'test'        | '2.1.0-rc.1'       | '1.0.0'                     | 3           | 6
	}

	private void commit(String name = '1.txt') {
		byte[] bytes = new byte[128]
		random.nextBytes(bytes)
		new File(grgit.repository.rootDir, name) << bytes
		grgit.add(patterns: [name])
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
		assert atCommit == newHead
		assert name == grgit.branch.current.name
	}

	private void merge(String name) {
		def currentHead = grgit.head()
		grgit.merge(head: name)
		println "Merged ${name}, now at ${grgit.head().abbreviatedId}"
	}
}
