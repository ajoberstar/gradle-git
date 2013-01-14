package org.ajoberstar.gradle.git.tasks

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.AnyObjectId
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.storage.file.FileRepository
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

public class GitBranchListTest extends Specification {
    def testDir = new File("build/tmp/test/gradle-git")
    Project project
    Git git

    def setup() {
        project = ProjectBuilder.builder().withName("GradleGitPluginTest").withProjectDir(testDir).build()
        git = Git.init().setDirectory(testDir).call()
        git.commit().setMessage("initial").call()

        //adding fake remote
        FileRepository repo = new FileRepositoryBuilder().setGitDir(new File(testDir, ".git")).build()
        def upd = repo.refDatabase.newUpdate("refs/remotes/remote1", false)
        upd.setNewObjectId(new AnyObjectId() {
            @Override
            ObjectId toObjectId() {
                return null
            }
        })
        upd.update()
    }

    def cleanup() {
        if (testDir.exists()) testDir.deleteDir()
    }

    def 'should list local branches by default'() {
        given:
        project.tasks.add(name: 'branchList', type: GitBranchList)
        when:
        project.branchList.execute()
        then:
        project.branchList.branches == ['refs/heads/master']
    }

    def 'when showAll then list local and remote branches'() {
        given:
        project.tasks.add(name: 'branchList', type: GitBranchList) {
            showAll = true
        }
        when:
        project.branchList.execute()
        then:
        project.branchList.branches == ['refs/heads/master', 'refs/remotes/remote1']
    }

}
