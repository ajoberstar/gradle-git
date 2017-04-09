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
package org.ajoberstar.gradle.git.release.semver

import org.ajoberstar.gradle.git.release.base.ReleaseVersion
import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Status
import org.ajoberstar.grgit.Tag
import org.ajoberstar.grgit.service.TagService

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class RebuildVersionStrategySpec extends Specification {
    RebuildVersionStrategy strategy = new RebuildVersionStrategy()
    Grgit grgit = GroovyMock()

    def getProject(Map properties) {
        Project p = ProjectBuilder.builder().withName("testproject").build()
        p.apply plugin: "org.ajoberstar.release-base"
        properties.each { k, v ->
            p.ext[k] = v
        }
        p
    }

    def 'selector returns false if repo is dirty'() {
        given:
        mockClean(false)
        Project project = getProject([:])
        mockTagsAtHead('v1.0.0')
        expect:
        !strategy.selector(project, grgit)
    }

    def 'selector returns false if any release properties are set'() {
        given:
        mockClean(true)
        Project project = getProject('release.scope': 'value')
        mockTagsAtHead('v1.0.0')
        expect:
        !strategy.selector(project, grgit)
    }

    def 'selector returns false if no version tag at HEAD'() {
        given:
        mockClean(true)
        Project project = getProject([:])
        mockTagsAtHead('non-version-tag')
        expect:
        !strategy.selector(project, grgit)
    }

    def 'selector returns true if rebuild is attempted'() {
        given:
        mockClean(true)
        Project project = getProject([:])
        mockTagsAtHead('v0.1.1', 'v1.0.0', '0.19.1')
        expect:
        strategy.selector(project, grgit)
    }

    def 'infer returns HEAD version is inferred and previous with create tag false'() {
        given:
        mockClean(true)
        Project project = getProject([:])
        mockTagsAtHead('v0.1.1', 'v1.0.0', '0.19.1')
        expect:
        strategy.infer(project, grgit) == new ReleaseVersion('1.0.0', '1.0.0', false)
    }

    private void mockTagsAtHead(String... tagNames) {
        Commit head = new Commit()
        grgit.head() >> head
        TagService tag = GroovyMock()
        grgit.tag >> tag
        tag.list() >> tagNames.collect { new Tag(commit: head, fullName: "refs/heads/${it}") }
    }

    private void mockClean(boolean clean) {
        Status status = GroovyMock()
        grgit.status() >> status
        status.clean >> clean
    }

    private void mockProperties(Map props) {
        project.properties.clear()
        project.properties.putAll(props)
    }
}
