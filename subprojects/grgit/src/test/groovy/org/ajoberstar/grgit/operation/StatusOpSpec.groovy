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
package org.ajoberstar.grgit.operation

import spock.lang.Specification

import org.ajoberstar.grgit.Repository
import org.ajoberstar.grgit.Status
import org.ajoberstar.grgit.service.RepositoryService
import org.ajoberstar.grgit.service.ServiceFactory

import org.eclipse.jgit.api.Git

import org.junit.Rule
import org.junit.rules.TemporaryFolder

class StatusOpSpec extends Specification {
	@Rule TemporaryFolder tempDir = new TemporaryFolder()

	RepositoryService grgit

	def setup() {
		File repoDir = tempDir.newFolder('repo')
		Git git = Git.init().setDirectory(repoDir).call()
		Repository repo = ServiceFactory.createRepository(repoDir)
		grgit = ServiceFactory.createService(repo)
		4.times { repoFile("${it}.txt") << "1" }
		git.add().addFilepattern('.').call()
		git.commit().setMessage('Test').call()
	}

	def 'with no changes all methods return empty list'() {
		expect:
		grgit.status() == new Status([] as Set, [] as Set, [] as Set,
			[] as Set, [] as Set, [] as Set)
	}

	def 'new unstaged file detected'() {
		given:
		repoFile('5.txt') << '5'
		repoFile('6.txt') << '6'
		expect:
		grgit.status() == new Status([] as Set, [] as Set, [] as Set,
			['5.txt', '6.txt'] as Set, [] as Set, [] as Set)
	}

	def 'unstaged modified files detected'() {
		given:
		repoFile('2.txt') << '2'
		repoFile('3.txt') << '3'
		expect:
		grgit.status() == new Status([] as Set, [] as Set, [] as Set,
			[] as Set, ['2.txt', '3.txt'] as Set, [] as Set)
	}

	def 'unstaged deleted files detected'() {
		given:
		assert repoFile('1.txt').delete()
		assert repoFile('2.txt').delete()
		expect:
		grgit.status() == new Status([] as Set, [] as Set, [] as Set,
			[] as Set, [] as Set, ['1.txt', '2.txt'] as Set)
	}

	def 'staged new files detected'() {
		given:
		repoFile('5.txt') << '5'
		repoFile('6.txt') << '6'
		when:
		grgit.repository.git.add().addFilepattern('.').call()
		then:
		grgit.status() == new Status(['5.txt', '6.txt'] as Set, [] as Set, [] as Set,
			[] as Set, [] as Set, [] as Set)
	}

	def 'staged modified files detected'() {
		given:
		repoFile('1.txt') << '5'
		repoFile('2.txt') << '6'
		when:
		grgit.repository.git.add().addFilepattern('.').call()
		then:
		grgit.status() == new Status([] as Set, ['1.txt', '2.txt'] as Set, [] as Set,
			[] as Set, [] as Set, [] as Set)
	}

	def 'staged new files detected'() {
		given:
		assert repoFile('3.txt').delete()
		assert repoFile('0.txt').delete()
		when:
		grgit.repository.git.add().addFilepattern('.').setUpdate(true).call()
		then:
		grgit.status() == new Status([] as Set, [] as Set, ['3.txt', '0.txt'] as Set,
			[] as Set, [] as Set, [] as Set)
	}

	private File repoFile(String path) {
		return new File(grgit.repository.rootDir, path)
	}
}
