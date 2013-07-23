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

class GitAddTest extends Specification {
    Project project

    def testDir = new File("build/tmp/test/${getClass().simpleName}")

    Git localGit

    GitAdd gitAddTask

    def setup() {
        localGit = Git.init().setDirectory(testDir).call()

        project = ProjectBuilder.builder().withName("GradleGitPluginTest").withProjectDir(testDir).build()
        gitAddTask = project.tasks.create('gitAdd', GitAdd)

        project.file("1.txt").withWriter { it << '111' }
        project.file("2.txt").withWriter { it << '222' }
        project.file('subdir').mkdir()
        project.file("subdir/3.txt").withWriter { it << '333' }
        project.file("subdir/4.txt").withWriter { it << '444' }
    }

    def cleanup() {
        if (testDir.exists()) testDir.deleteDir()
    }

    def 'should add all files if nothing included or excluded used'() {
        when:
        project.gitAdd.execute()
        then:
        localGit.status().call().added == (['1.txt', '2.txt', 'subdir/3.txt', 'subdir/4.txt'] as Set<String>)
    }

    def 'should add single file, if explicitly included'() {
        given:
        gitAddTask.include filePath
        when:
        project.gitAdd.execute()
        then:
        localGit.status().call().added == ([filePath] as Set<String>)
        where:
        filePath << ['1.txt', 'subdir/3.txt']
    }

    def 'should add all contents of dir if included with *'() {
        given:
        gitAddTask.include 'subdir/*'
        when:
        project.gitAdd.execute()
        then:
        localGit.status().call().added == (['subdir/3.txt', 'subdir/4.txt'] as Set<String>)
    }

    def 'should add specific files if included'() {
        given:
        gitAddTask.include '2.txt'
        gitAddTask.include 'subdir/4.txt'
        when:
        project.gitAdd.execute()
        then:
        localGit.status().call().added == (['2.txt', 'subdir/4.txt'] as Set<String>)   
    }

    def 'should not add directory if included without a trailing slash'() {
        given:
        gitAddTask.include 'subdir'
        when:
        project.gitAdd.execute()
        then:
        localGit.status().call().added == ([] as Set<String>)      
    }

    def 'should add all contents of directory if included with a trailing slash'() {
        given:
        gitAddTask.include 'subdir/'
        when:
        project.gitAdd.execute()
        then:
        localGit.status().call().added == (['subdir/3.txt', 'subdir/4.txt'] as Set<String>)      
    }
}
