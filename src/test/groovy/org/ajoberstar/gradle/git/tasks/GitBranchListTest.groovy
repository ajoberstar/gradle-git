package org.ajoberstar.gradle.git.tasks

import org.ajoberstar.gradle.git.api.Branch
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.storage.file.FileRepository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification

public class GitBranchListTest extends Specification {

	Project project

	static def testDir = new File("build/tmp/test/gradle-git")

	@Shared File localRepo
	@Shared Git localGit

	def setupSpec() {
		if (testDir.exists()) testDir.deleteDir()
		testDir.mkdirs()

		//setup fake remote, actually from local disk
		def remoteRepo = new File(testDir, "remote")
		Git remoteGit = Git.init().setDirectory(remoteRepo).call()
		remoteGit.getRepository().getConfig().setString("receive", null, "denyCurrentBranch", "ignore")
		remoteGit.getRepository().getConfig().save()
		remoteGit.commit().setMessage("initial").call()
		remoteGit.branchCreate().setName("remoteBranch1").call()
		new File(remoteRepo, "test.txt").withWriter {it << '111'}
		remoteGit.add().addFilepattern("test.txt").call()
		remoteGit.commit().setMessage("firstCommit").call()
		remoteGit.tag().setName("firstTag").call()
		new File(remoteRepo, "test.txt").withWriter {it << '222'}
		remoteGit.commit().setAll(true).setMessage("secondCommit").call()

		//clone local repo from fake remote
		localRepo = new File(testDir, 'local')
		localGit = Git.cloneRepository().setDirectory(localRepo).setURI(remoteRepo.canonicalPath).call()
	}

	def cleanupSpec() {
		if (testDir.exists()) testDir.deleteDir()
	}

	def setup() {
		project = ProjectBuilder.builder().withName("GradleGitPluginTest").withProjectDir(localRepo).build()
		project.tasks.add(name: 'branchList', type: GitBranchList)
	}

	def 'when work on branch then workingBranch reflects current branch info'() {
		when:
		project.branchList.execute()
		then:
		project.branchList.workingBranch == project.branchList.branches.find {it.name == 'master'}
	}

	def 'when work on detached state then workingBranch reflects current commit info'() {
		given:
		localGit.checkout().setName("firstTag").call()
		def tag = localGit.tagList().call().find {Repository.shortenRefName(it.name)  == 'firstTag'}
		when:
		project.branchList.execute()
		Branch current = project.branchList.workingBranch
		then:
		!current.name && !current.refName && current.commit
	}

	def 'should list local branches by default'() {
		when:
		project.branchList.execute()
		then:
		project.branchList.branches*.refName == ['refs/heads/master']
	}

	def 'when branch type ALL then list local and remote branches'() {
		given:
		project.branchList.branchType = GitBranchList.BranchType.ALL
		when:
		project.branchList.execute()
		then:
		project.branchList.branches*.refName == ['refs/heads/master', 'refs/remotes/origin/master', 'refs/remotes/origin/remoteBranch1']
	}

	def 'when branch type REMOTE then list remote branches'() {
		given:
		project.branchList.branchType = GitBranchList.BranchType.REMOTE
		when:
		project.branchList.execute()
		then:
		project.branchList.branches*.refName == ['refs/remotes/origin/master', 'refs/remotes/origin/remoteBranch1']
	}
}
