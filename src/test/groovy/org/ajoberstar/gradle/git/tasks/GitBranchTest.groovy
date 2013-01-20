package org.ajoberstar.gradle.git.tasks

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.storage.file.FileRepository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

public class GitBranchTest extends Specification {
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
        project.tasks.add(name: 'branchCreate', type: GitBranch)
        when:
        project.branchCreate.execute()
        then:
        thrown GradleException
    }

    def 'should create branch with passed name'() {
        given:
        project.tasks.add(name: 'branchCreate', type: GitBranch) {
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
        project.tasks.add(name: 'branchCreate', type: GitBranch) {
            branchName = 'branch1'
            mode = GitBranch.Mode.TRACK
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
        project.tasks.add(name: 'branchCreate', type: GitBranch) {
            branchName = 'branch1'
            startPoint = 'startBranch'
            mode = GitBranch.Mode.TRACK
        }
        when:
        project.branchCreate.execute()
        then:
        fileRepository.config.getString("branch", "branch1", 'remote') == '.'
        fileRepository.config.getString("branch", "branch1", 'merge') == 'refs/heads/startBranch'
    }

    def 'when configured then create non tracking branch'() {
        given:
        project.tasks.add(name: 'branchCreate', type: GitBranch) {
            branchName = 'branch1'
            mode = GitBranch.Mode.NO_TRACK
        }
        when:
        project.branchCreate.execute()
        then:
        fileRepository.config.getNames("branch", "branch1").size() == 0
    }

    def 'when create branch tha already exist w/o `force` then exception'() {
        given:
        git.branchCreate().setName("branch1").call()
        project.tasks.add(name: 'branchCreate', type: GitBranch) {
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
        project.tasks.add(name: 'branchCreate', type: GitBranch) {
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
        project.tasks.add(name: 'branchCreate', type: GitBranch) {
            branchName = 'branch1'
            force = true
        }
        when:
        project.branchCreate.execute()
        then:
        notThrown GradleException
    }
}
