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
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification;

class GitStatusTest extends Specification {

    def testDir = new File("build/tmp/test/gradle-git/status")
    Project project
    Git git

	def setup() {
		project = ProjectBuilder.builder().withName("GradleGitPluginTest").withProjectDir(testDir).build()
		project.tasks.add(name: "gitStatus", type: GitStatus)
		git = Git.init().setDirectory(testDir).call()
		project.file("existing.txt").withWriter { it << 'test' }
		git.add().addFilepattern("existing.txt").call()
		git.commit().setMessage("existing").call()
	}

	def cleanup() {
		if (testDir.exists()) testDir.deleteDir()
	}

	def 'should detect untracked files'() {
		given:
		project.file("new.txt").withWriter { it << 'test' }
		when:
		project.gitStatus.execute()
		then:
		project.gitStatus.untracked.files == project.files('new.txt').files
	}

	def 'should detect modified files'() {
		given:
		project.file("existing.txt").withWriter { it << 'update' }
		when:
		project.gitStatus.execute()
		then:
		project.gitStatus.modified.files == project.files('existing.txt').files
	}

	def 'should detect changed files'() {
		given:
		project.file("existing.txt").withWriter { it << 'update' }
		git.add().addFilepattern("existing.txt").call()
		when:
		project.gitStatus.execute()
		then:
		project.gitStatus.changed.files == project.files('existing.txt').files
	}

	def 'should detect added files'() {
		given:
		project.file("new.txt").withWriter { it << 'update' }
		git.add().addFilepattern("new.txt").call()
		when:
		project.gitStatus.execute()
		then:
		project.gitStatus.added.files == project.files('new.txt').files
	}

	def 'should detect missing files'() {
		given:
		new File(testDir, "existing.txt").delete()
		when:
		project.gitStatus.execute()
		then:
		project.gitStatus.missing.files == project.files('existing.txt').files
	}
}
