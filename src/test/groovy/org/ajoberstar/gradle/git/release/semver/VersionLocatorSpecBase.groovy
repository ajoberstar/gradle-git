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

import spock.lang.Specification

//import java.nio.file.Files
import java.security.SecureRandom

//import com.github.zafarkhaja.semver.Version

import org.ajoberstar.grgit.Grgit

import spock.lang.Shared
import spock.lang.Unroll

class VersionLocatorSpecBase extends Specification {

	@Shared File repoDir

	@Shared Grgit grgit

	@Shared SecureRandom random = new SecureRandom()

	protected void commit() {
		byte[] bytes = new byte[128]
		random.nextBytes(bytes)
		new File(grgit.repository.rootDir, '1.txt') << bytes
		grgit.add(patterns: ['1.txt'])
		def commit = grgit.commit(message: 'do')
		println "Created commit: ${commit.abbreviatedId}"
	}

	protected void addBranch(String name) {
		def currentHead = grgit.head()
		def currentBranch = grgit.branch.current
		def newBranch = grgit.branch.add(name: name)
		def atCommit = grgit.resolve.toCommit(newBranch.fullName)
		println "Added new branch ${name} at ${atCommit.abbreviatedId}"
		assert currentBranch == grgit.branch.current
		assert currentHead == atCommit
	}

	protected void addTag(String name) {
		def currentHead = grgit.head()
		def newTag = grgit.tag.add(name: name)
		def atCommit = grgit.resolve.toCommit(newTag.fullName)
		println "Added new tag ${name} at ${atCommit.abbreviatedId}"
		assert currentHead == atCommit
	}

	protected void checkout(String name) {
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
