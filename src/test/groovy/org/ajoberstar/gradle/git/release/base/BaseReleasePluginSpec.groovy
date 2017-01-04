/*
 * Copyright 2012-2017 the original author or authors.
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
package org.ajoberstar.gradle.git.release.base

import org.ajoberstar.grgit.Branch
import org.ajoberstar.grgit.BranchStatus
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.service.BranchService
import org.ajoberstar.grgit.service.TagService

import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification

class BaseReleasePluginSpec extends Specification {
    Project project = ProjectBuilder.builder().build()

    def setup() {
        project.plugins.apply('org.ajoberstar.release-base')
    }

    def 'prepare task succeeds if branch is up to date'() {
        given:
        Grgit repo = GroovyMock()
        BranchService branch = GroovyMock()
        repo.branch >> branch
        branch.current >> new Branch(fullName: 'refs/heads/master')
        branch.status([branch: 'refs/heads/master']) >> new BranchStatus(behindCount: 0)

        project.release {
            grgit = repo
        }
        when:
        project.tasks.prepare.execute()
        then:
        notThrown(GradleException)
        1 * repo.fetch([remote: 'origin'])
    }

    def 'prepare task fails if branch is behind'() {
        given:
        Grgit repo = GroovyMock()
        BranchService branch = GroovyMock()
        repo.branch >> branch
        branch.current >> new Branch(fullName: 'refs/heads/master', trackingBranch: new Branch(fullName: 'refs/remotes/origin/master'))
        branch.status([branch: 'refs/heads/master']) >> new BranchStatus(behindCount: 2)

        project.release {
            grgit = repo
        }
        when:
        project.tasks.prepare.execute()
        then:
        thrown(GradleException)
        1 * repo.fetch([remote: 'origin'])
    }

    def 'release task pushes branch and tag if created'() {
        given:
        Grgit repo = GroovyMock()
        BranchService branch = GroovyMock()
        repo.branch >> branch
        TagService tag = GroovyMock()
        repo.tag >> tag
        branch.current >> new Branch(fullName: 'refs/heads/master', trackingBranch: new Branch(fullName: 'refs/remotes/origin/master'))
        project.release {
            versionStrategy([
                getName: { 'a' },
                selector: {proj, repo2 -> true },
                infer: {proj, repo2 -> new ReleaseVersion('1.2.3', null, true)}] as VersionStrategy)
            grgit = repo
        }
        when:
        project.tasks.release.execute()
        then:
        1 * repo.push([remote: 'origin', refsOrSpecs: ['refs/heads/master', 'v1.2.3']])
    }

    def 'release task pushes branch but not tag if it was not created'() {
        given:
        Grgit repo = GroovyMock()
        BranchService branch = GroovyMock()
        repo.branch >> branch
        TagService tag = GroovyMock()
        repo.tag >> tag
        branch.current >> new Branch(fullName: 'refs/heads/master', trackingBranch: new Branch(fullName: 'refs/remotes/origin/master'))
        project.release {
            versionStrategy([
                getName: { 'a' },
                selector: {proj, repo2 -> true },
                infer: {proj, repo2 -> new ReleaseVersion('1.2.3', null, false)}] as VersionStrategy)
            grgit = repo
        }
        when:
        project.tasks.release.execute()
        then:
        1 * repo.push([remote: 'origin', refsOrSpecs: ['refs/heads/master']])
    }

    def 'release task skips push if on detached head'() {
        given:
        Grgit repo = GroovyMock()
        BranchService branch = GroovyMock()
        repo.branch >> branch
        TagService tag = GroovyMock()
        repo.tag >> tag
        branch.current >> new Branch(fullName: 'HEAD')
        project.release {
            versionStrategy([
                getName: { 'a' },
                selector: {proj, repo2 -> true },
                infer: {proj, repo2 -> new ReleaseVersion('1.2.3', null, false)}] as VersionStrategy)
            grgit = repo
        }
        when:
        project.tasks.release.execute()
        then:
        0 * repo.push(_)
    }
}
