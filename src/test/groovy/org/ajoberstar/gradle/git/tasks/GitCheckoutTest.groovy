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
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

import static org.eclipse.jgit.lib.Constants.MASTER

class GitCheckoutTest extends Specification {

    Project project

    def testDir = new File("build/tmp/test/${getClass().simpleName}")
    Git localGit

    def setup() {
        localGit = Git.init().setDirectory(testDir).call()

        project = ProjectBuilder.builder().withName("GradleGitPluginTest").withProjectDir(testDir).build()
        project.tasks.add(name: "gitCheckout", type: GitCheckout)

        project.file("testMaster.txt").withWriter { it << 'testMaster' }
        localGit.add().addFilepattern("testMaster.txt").call()
        localGit.commit().setMessage("testMaster").call()

        localGit.checkout().setCreateBranch(true).setName("branch").call()

        project.file("testBranch.txt").withWriter { it << 'testBranch' }
        localGit.add().addFilepattern("testBranch.txt").call()
        localGit.commit().setMessage("testBranch").call()

        localGit.checkout().setName(MASTER).call()
    }

    def cleanup() {
        if (testDir.exists()) testDir.deleteDir()
    }

    def 'should checkout master branch by default'() {
        given: 'working branch is not master'
        localGit.checkout().setName('branch').call()
        when:
        project.gitCheckout.execute()
        then:
        localGit.getRepository().getBranch() == MASTER
    }

    def 'when name is set then checkout particular branch'() {
        given: 'working branch master'
        project.gitCheckout.branchName = 'branch'
        when:
        project.gitCheckout.execute()
        then:
        localGit.repository.branch == 'branch'
    }

    def 'when checkout missing branch then exception'() {
        given: 'working branch master'
        project.gitCheckout.branchName = 'missing'
        when:
        project.gitCheckout.execute()
        then:
        thrown GradleException
    }

    def 'when checkout missing branch with createBranch flag then new branch created'() {
        given:
        project.gitCheckout {
            branchName = 'missing'
            createBranch = true
        }
        when:
        project.gitCheckout.execute()
        then:
        localGit.branchList().call()*.name.any { it == 'refs/heads/missing' }
    }

    def 'when starting point configured then checkout certain commit'() {
        given: 'change and commit file in working branch'
        project.file("testMaster.txt").withWriter { it << 'testMaster2' }
        localGit.commit().setAll(true).setMessage("testMaster").call()
        when: 'check out one commit ago'
        project.gitCheckout {
            startPoint = 'HEAD~1'
            include 'testMaster.txt'
        }
        project.gitCheckout.execute()
        then: 'working dir has 1st version of file'
        localGit.repository.workTree.listFiles().any { it.name == 'testMaster.txt' && it.text == 'testMaster' }
    }

    def 'when file patterns configured then checkout only certain files'() {
        given: 'change and commit files in working branch'
        project.file("testMaster.txt").withWriter { it << 'testMaster2' }
        localGit.commit().setAll(true).setMessage("testMaster").call()

        project.file("testNew.txt").withWriter { it << 'testNew' }
        localGit.add().addFilepattern("testNew.txt").call()
        localGit.commit().setAll(true).setMessage("testNew").call()

        when: 'check out one commit ago'
        project.gitCheckout {
            startPoint = 'HEAD~2'
            include 'testMaster.txt'
        }
        project.gitCheckout.execute()
        then: 'working dir has 1st version of file'
        localGit.repository.workTree.listFiles().any { it.name == 'testMaster.txt' && it.text == 'testMaster' }
    }
}
