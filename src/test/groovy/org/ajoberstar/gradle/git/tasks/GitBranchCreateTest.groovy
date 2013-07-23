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
package org.ajoberstar.gradle.git.tasks

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

public class GitBranchCreateTest extends Specification {
	def testDir = new File("build/tmp/test/gradle-git")
	Project project
	Git git
	FileRepository fileRepository

	def setup() {
		project = ProjectBuilder.builder().withName("GradleGitPluginTest").withProjectDir(testDir).build()
		project.file("test.txt").withWriter { it << 'test' }

		git = Git.init().setDirectory(testDir).call()
		git.add().addFilepattern("test.txt").call()
		git.commit().setMessage("initial").call()

		fileRepository = new FileRepositoryBuilder().setGitDir(new File(testDir, ".git")).build()
	}

	def cleanup() {
		if (testDir.exists()) testDir.deleteDir()
	}

	def 'should wrap git errors with GradleException'() {
		given:
		project.tasks.create(name: 'branchCreate', type: GitBranchCreate)
		when:
		project.branchCreate.execute()
		then:
		thrown GradleException
	}

	def 'should create branch with passed name'() {
		given:
		project.tasks.create(name: 'branchCreate', type: GitBranchCreate) {
			branchName = 'branch1'
		}
		when:
		project.branchCreate.execute()
		then: 'name is correct'
		git.branchList().call().find { it.name =~ 'branch1' }
		and: 'branch is not a tracking branch'
		fileRepository.config.getNames("branch", "branch1").size() == 0
	}

	def 'when create tracking branch `master` should be used by default'() {
		given:
		project.tasks.create(name: 'branchCreate', type: GitBranchCreate) {
			branchName = 'branch1'
			mode = GitBranchCreate.Mode.TRACK
		}
		when:
		project.branchCreate.execute()
		then:
		fileRepository.config.getString("branch", "branch1", 'remote') == '.'
		fileRepository.config.getString("branch", "branch1", 'merge') == 'refs/heads/master'
	}


	def 'when start point given then create tracking branch from it'() {
		given:
		git.branchCreate().setName("startBranch").call()
		project.tasks.create(name: 'branchCreate', type: GitBranchCreate) {
			branchName = 'branch1'
			startPoint = 'startBranch'
			mode = GitBranchCreate.Mode.TRACK
		}
		when:
		project.branchCreate.execute()
		then:
		fileRepository.config.getString("branch", "branch1", 'remote') == '.'
		fileRepository.config.getString("branch", "branch1", 'merge') == 'refs/heads/startBranch'
	}

	def 'when configured then create non tracking branch'() {
		given:
		project.tasks.create(name: 'branchCreate', type: GitBranchCreate) {
			branchName = 'branch1'
			mode = GitBranchCreate.Mode.NO_TRACK
		}
		when:
		project.branchCreate.execute()
		then:
		fileRepository.config.getNames("branch", "branch1").size() == 0
	}

	def 'when create branch tha already exist w/o `force` then exception'() {
		given:
		git.branchCreate().setName("branch1").call()
		project.tasks.create(name: 'branchCreate', type: GitBranchCreate) {
			branchName = 'branch1'
		}
		when:
		project.branchCreate.execute()
		then:
		thrown GradleException
	}

	def 'when create branch wish existing name w/o `force` then exception'() {
		given:
		git.branchCreate().setName("branch1").call()
		project.tasks.create(name: 'branchCreate', type: GitBranchCreate) {
			branchName = 'branch1'
		}
		when:
		project.branchCreate.execute()
		then:
		thrown GradleException
	}

	def 'when create branch wish existing name w/ `force` then OK'() {
		given:
		git.branchCreate().setName("branch1").call()
		project.tasks.create(name: 'branchCreate', type: GitBranchCreate) {
			branchName = 'branch1'
			force = true
		}
		when:
		project.branchCreate.execute()
		then:
		notThrown GradleException
	}
}
