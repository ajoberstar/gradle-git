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
import org.ajoberstar.grgit.Grgit
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.nio.file.Files
import java.security.SecureRandom

class NearestVersionLocatorSpec extends Specification {
	private static final Logger logger = LoggerFactory.getLogger(NearestVersionLocatorSpec)

	@Shared File repoDir

	@Shared Grgit grgit

	@Shared SecureRandom random = new SecureRandom()

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
		addTag('not-a-version')
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
		addTag('also-not-a-version')
	}

	def cleanupSpec() {
		cleanupRepo(repoDir)
	}

	@Unroll('when on #head, locator finds normal #normal with distance #distance and nearest #any')
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

	public static void cleanupRepo(File repoDir) {
		assert !repoDir.exists() || repoDir.deleteDir()
	}

	public void commit() {
		commit(random, grgit)
	}

	public static void commit(Random random, Grgit grgit) {
		byte[] bytes = new byte[128]
		random.nextBytes(bytes)
		new File(grgit.repository.rootDir, '1.txt') << bytes
		grgit.add(patterns: ['1.txt'])
		def commit = grgit.commit(message: 'do')
		logger.debug("Created commit: ${commit.abbreviatedId}")
	}

	public void addBranch(String name) {
		addBranch(name, grgit)
	}

	public static void addBranch(String name, Grgit grgit) {
		def currentHead = grgit.head()
		def currentBranch = grgit.branch.current
		def newBranch = grgit.branch.add(name: name)
		def atCommit = grgit.resolve.toCommit(newBranch.fullName)
		logger.debug("Added new branch ${name} at ${atCommit.abbreviatedId}")
		assert currentBranch == grgit.branch.current
		assert currentHead == atCommit
	}

	public void addTag(String name) {
		addTag(name, grgit)
	}

	public static void addTag(String name, Grgit grgit) {
		def currentHead = grgit.head()
		def newTag = grgit.tag.add(name: name)
		def atCommit = grgit.resolve.toCommit(newTag.fullName)
		logger.debug("Added new tag ${name} at ${atCommit.abbreviatedId}")
		assert currentHead == atCommit
	}

	public void checkout(String name) {
		checkout(name, grgit)
	}

	public static void checkout(String name, Grgit grgit) {
		def currentHead = grgit.head()
		grgit.checkout(branch: name)
		def atCommit = grgit.resolve.toCommit(name)
		def newHead = grgit.head()
		logger.debug("Checkout out ${name}, which is at ${atCommit.abbreviatedId}")
		assert currentHead != grgit.head()
		assert atCommit == newHead
		assert name == grgit.branch.current.name
	}
}
