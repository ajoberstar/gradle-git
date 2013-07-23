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
import spock.lang.Specification

class GitCommitTest extends Specification {
    Project project

    def testDir = new File("build/tmp/test/${getClass().simpleName}")

    Git localGit

    GitCommit gitCommitTask

    def setup() {
        localGit = Git.init().setDirectory(testDir).call()

        project = ProjectBuilder.builder().withName("GradleGitPluginTest").withProjectDir(testDir).build()
        gitCommitTask = project.tasks.create("gitCommit", GitCommit)
        gitCommitTask.message = 'test'

        project.file("1.txt").withWriter { it << '111' }
        project.file("2.txt").withWriter { it << '222' }
        localGit.add().addFilepattern("1.txt").addFilepattern("2.txt").call()
    }

    def cleanup() {
        if (testDir.exists()) testDir.deleteDir()
    }

    def 'should commit only explicitly included files'() {
        given:
        gitCommitTask.include('1.txt')
        when:
        project.gitCommit.execute()
        then:
        localGit.status().call().added == (['2.txt'] as Set<String>)
    }

    def 'should commit only explicitly not excluded files'() {
        given:
        gitCommitTask.include('*.txt')
        gitCommitTask.exclude('2.txt')
        when:
        project.gitCommit.execute()
        then:
        localGit.status().call().added == (['2.txt'] as Set<String>)
    }

    def 'when no files config then commit all files'() {
        when:
        project.gitCommit.execute()
        then:
        localGit.status().call().added.size() == 0
    }
}
