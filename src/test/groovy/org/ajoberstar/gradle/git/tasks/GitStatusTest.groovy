package org.ajoberstar.gradle.git.tasks

import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification;

class GitStatusTest extends Specification {

	def testDir = new File("build/tmp/test/gradle-git")
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
