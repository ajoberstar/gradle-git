package org.ajoberstar.grgit.util

import org.ajoberstar.grgit.Repository
import org.ajoberstar.grgit.service.ServiceFactory
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.merge.MergeStrategy
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class JGitUtilSpec extends Specification {
	@Rule TemporaryFolder tempDir = new TemporaryFolder()

	Repository repo
	List commits = []

	def 'resolveObject works for branch name'() {
		expect:
		JGitUtil.resolveObject(repo, 'master') == commits[3]
	}

	def 'resolveObject works for full commit hash'() {
		expect:
		JGitUtil.resolveObject(repo, ObjectId.toString(commits[0])) == commits[0]
	}

	def 'resolveObject works for abbreviated commit hash'() {
		expect:
		JGitUtil.resolveObject(repo, ObjectId.toString(commits[0])[0..5]) == commits[0]
	}

	def 'resolveObject works for full ref name'() {
		expect:
		JGitUtil.resolveObject(repo, 'refs/heads/master') == commits[3]
	}

	def 'resolveObject works for HEAD'() {
		expect:
		JGitUtil.resolveObject(repo, 'HEAD') == commits[3]
	}

	def 'resolveObject works for parent commit'() {
		expect:
		JGitUtil.resolveObject(repo, 'master^') == commits[1]
	}

	def 'resolveObject works for current commit'() {
		expect:
		JGitUtil.resolveObject(repo, 'master^0') == commits[3]	
	}

	def 'resolveObject works for n-th parent'() {
		expect:
		JGitUtil.resolveObject(repo, 'master^2') == commits[2]
	}

	def 'resolveObject works for the n-th ancestor'() {
		expect:
		JGitUtil.resolveObject(repo, 'master~2') == commits[0]
	}

	def setup() {
		File repoDir = tempDir.newFolder('repo')
		Git git = Git.init().setDirectory(repoDir).call()
		File testFile = new File(repoDir, '1.txt')
		testFile << '1\n'
		git.add().addFilepattern(testFile.name).call()
		commits << git.commit().setMessage('first commit').call()
		testFile << '2\n'
		git.add().addFilepattern(testFile.name).call()
		commits << git.commit().setMessage('second commit').call()
		git.checkout().setName(ObjectId.toString(commits[0])).call()
		testFile << '3\n'
		git.add().addFilepattern(testFile.name).call()
		commits << git.commit().setMessage('third commit').call()
		git.checkout().setName('master').call()
		commits << git.merge().include(commits[2]).setStrategy(MergeStrategy.OURS).call().newHead
		repo = ServiceFactory.createRepository(repoDir)
	}
}
