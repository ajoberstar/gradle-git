package org.ajoberstar.gradle.git.tasks

import org.eclipse.jgit.api.Git
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification

import static org.eclipse.jgit.lib.Constants.DEFAULT_REMOTE_NAME

class GitBranchTrackingStatusTest extends Specification {
    Project project

    static def testDir = new File("build/tmp/test/gradle-git")

    @Shared File localRepo
    @Shared File remoteRepo

    @Shared Git localGit
    @Shared Git remoteGit

    GitBranchTrackingStatus task

    def setupSpec() {
        if (testDir.exists()) testDir.deleteDir()
        testDir.mkdirs()

        //setup fake remote, actually from local disk
        remoteRepo = new File(testDir, "remote")
        remoteGit = Git.init().setDirectory(remoteRepo).call()
        remoteGit.getRepository().getConfig().setString("receive", null, "denyCurrentBranch", "ignore")
        remoteGit.getRepository().getConfig().save()
        new File(remoteRepo, "test.txt").withWriter { it << '111' }
        remoteGit.add().addFilepattern("test.txt").call()
        remoteGit.commit().setMessage("firstCommit").call()

        //clone local repo from fake remote
        localRepo = new File(testDir, 'local')
        localGit = Git.cloneRepository().setDirectory(localRepo).setURI(remoteRepo.canonicalPath).call()
    }

    def cleanupSpec() {
        if (testDir.exists()) testDir.deleteDir()
    }

    def setup() {
        project = ProjectBuilder.builder().withName("GradleGitPluginTest").withProjectDir(localRepo).build()
        task = project.tasks.add(name: 'branchTrackingStatus', type: GitBranchTrackingStatus)
    }

    def cleanup() {
        localGit.push().setForce(true).call()
        localGit.fetch().setRemote(DEFAULT_REMOTE_NAME).call()
    }

    def 'should set behindCount when remote has commit missing in local `master`'() {
        given:
        new File(remoteRepo, "test.txt").withWriter { it << "222" }
        remoteGit.commit().setAll(true).setMessage("message").call()
        localGit.fetch().setRemote(DEFAULT_REMOTE_NAME).call()
        when:
        task.execute()
        then:
        task.trackingStatus.behindCount == 1
        task.trackingStatus.aheadCount == 0
        task.trackingStatus.remoteBranchRef.contains("master")
    }

    def 'should set aheadCount when local `master` has commit missing in remote'() {
        given:
        new File(localRepo, "test.txt").withWriter { it << "333" }
        localGit.commit().setAll(true).setMessage("message").call()
        when:
        task.execute()
        then:
        task.trackingStatus.aheadCount == 1
        task.trackingStatus.behindCount == 0
        task.trackingStatus.remoteBranchRef.contains("master")
    }

    def 'when `localBranch` present in task then it name used for getting tracking info instead of `master`'() {
        given:
        task.localBranch = 'non-exisiting-branch'
        when:
        task.execute()
        then:
        task.trackingStatus.remoteBranchRef == null
    }

}
