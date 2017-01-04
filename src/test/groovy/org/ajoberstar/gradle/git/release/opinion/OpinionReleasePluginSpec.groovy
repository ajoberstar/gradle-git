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
package org.ajoberstar.gradle.git.release.opinion

import org.ajoberstar.gradle.git.release.base.ReleaseVersion
import org.ajoberstar.gradle.git.release.semver.RebuildVersionStrategy
import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.exception.GrgitException
import org.ajoberstar.grgit.service.ResolveService

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

import spock.lang.Specification

class OpinionReleasePluginSpec extends Specification {
    Project project = ProjectBuilder.builder().build()
    ReleaseVersion version = new ReleaseVersion(version: '1.2.3', previousVersion: '1.2.2')

    def 'plugin adds correct strategies'() {
        given:
        project.plugins.apply('org.ajoberstar.release-opinion')
        expect:
        project.release.versionStrategies == [RebuildVersionStrategy.INSTANCE, Strategies.DEVELOPMENT, Strategies.PRE_RELEASE, Strategies.FINAL]
        project.release.defaultVersionStrategy == Strategies.DEVELOPMENT
    }

    def 'plugin tag strategy creates correct message if previous tag exists'() {
        given:
        project.plugins.apply('org.ajoberstar.release-opinion')
        Grgit grgit = GroovyMock()
        project.release.grgit = grgit
        ResolveService resolve = Mock()
        (1.._) * grgit.resolve >> resolve
        1 * resolve.toCommit('v1.2.2^{commit}') >> new Commit(shortMessage: 'Commit 1')
        1 * grgit.log([includes: ['HEAD'], excludes: ['v1.2.2^{commit}']]) >> [
            new Commit(shortMessage: 'Commit 2'),
            new Commit(shortMessage: 'Next commit')]
        0 * grgit._
        expect:
        project.release.tagStrategy.generateMessage(version).trim() == '''
Release of 1.2.3

- Commit 2
- Next commit
'''.trim()
    }

    def 'plugin tag strategy creates correct message if previous tag exists and no prefix with v'() {
        given:
        project.plugins.apply('org.ajoberstar.release-opinion')
        Grgit grgit = GroovyMock()
        project.release.grgit = grgit
        project.release.tagStrategy.prefixNameWithV = false
        ResolveService resolve = Mock()
        (1.._) * grgit.resolve >> resolve
        1 * resolve.toCommit('1.2.2^{commit}') >> new Commit(shortMessage: 'Commit 1')
        1 * grgit.log([includes: ['HEAD'], excludes: ['1.2.2^{commit}']]) >> [
                new Commit(shortMessage: 'Commit 2'),
                new Commit(shortMessage: 'Next commit')]
        0 * grgit._
        expect:
        project.release.tagStrategy.generateMessage(version).trim() == '''
Release of 1.2.3

- Commit 2
- Next commit
'''.trim()
    }

    def 'plugin tag strategy creates correct message if previous tag does not exist'() {
        given:
        project.plugins.apply('org.ajoberstar.release-opinion')
        Grgit grgit = GroovyMock()
        project.release.grgit = grgit
        ResolveService resolve = Mock()
        (1.._) * grgit.resolve >> resolve
        1 * grgit.resolve.toCommit('v1.2.2^{commit}') >> { throw new GrgitException('fail') }
        1 * grgit.log([includes: ['HEAD'], excludes: []]) >> [
            new Commit(shortMessage: 'Commit 1'),
            new Commit(shortMessage: 'Commit 2'),
            new Commit(shortMessage: 'Next commit')]
        0 * grgit._
        expect:
        project.release.tagStrategy.generateMessage(version).trim() == '''
Release of 1.2.3

- Commit 1
- Commit 2
- Next commit
'''.trim()
    }
}
