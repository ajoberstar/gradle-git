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
import org.ajoberstar.grgit.service.RepositoryService
import org.ajoberstar.grgit.service.ServiceFactory

import org.eclipse.jgit.api.Git

import org.junit.Rule
import org.junit.rules.TemporaryFolder

class RmOpSpec extends Specification {
	@Rule TemporaryFolder tempDir = new TemporaryFolder()

	RepositoryService grgit

	def setup() {
		File repoDir = tempDir.newFolder('repo')
		Git git = Git.init().setDirectory(repoDir).call()
		Repository repo = ServiceFactory.createRepository(repoDir)
		grgit = ServiceFactory.createService(repo)

		repoFile('1.bat') << '1'
		repoFile('something/2.txt') << '2'
		repoFile('test/3.bat') << '3'
		repoFile('test/4.txt') << '4'
		repoFile('test/other/5.txt') << '5'
		grgit.stage.add(patterns:['.'])
		grgit.repository.git.commit().setMessage('Test').call()
	}

	def 'reset soft changes HEAD only'() {}

	def 'reset mixed changes HEAD and index'() {}

	def 'reset hard changes HEAD, index, and working tree'() {}

	def 'reset merge changes HEAD, index, and working tree but not unstaged changes'() {}

	def 'reset merge aborts when unstaged changes to file that differs between commit and index'() {}

	def 'reset keep changes HEAD, index, and working tree if no unstaged changes'() {}

	def 'reset keep aborts if any unstaged changes different from commit'() {}

	private File repoFile(String path, boolean makeDirs = true) {
		def file = new File(grgit.repository.rootDir, path)
		if (makeDirs) file.parentFile.mkdirs()
		return file
	}
}
