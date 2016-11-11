/*
 * Copyright 2012-2015 the original author or authors.
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

import org.ajoberstar.grgit.Grgit
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class ReleasePluginExtensionSpec extends Specification {
    def 'infers default version if selector returns false for all but default'() {
        given:
        Project project = ProjectBuilder.builder().build()
        ReleasePluginExtension extension = new ReleasePluginExtension(project)
        extension.grgit = GroovyMock(Grgit)
        extension.versionStrategy([
            getName: { 'b' },
            selector: { proj, grgit -> false },
            infer: { proj, grgit -> new ReleaseVersion('1.0.0', null, true) }] as VersionStrategy)
        extension.defaultVersionStrategy = [
            getName: { 'a' },
            selector: { proj, grgit -> true },
            infer: { proj, grgit -> new ReleaseVersion('1.2.3', null, true) }] as VersionStrategy
        expect:
        project.version.toString() == '1.2.3'
    }

    def 'infers using first strategy selector returns true for'() {
        Project project = ProjectBuilder.builder().build()
        ReleasePluginExtension extension = new ReleasePluginExtension(project)
        extension.grgit = GroovyMock(Grgit)
        extension.versionStrategy([
            getName: { 'b' },
            selector: { proj, grgit -> false },
            infer: { proj, grgit -> new ReleaseVersion('1.0.0', null, true) }] as VersionStrategy)
        extension.versionStrategy([
            getName: { 'a' },
            selector: { proj, grgit -> true },
            infer: { proj, grgit -> new ReleaseVersion('1.2.3', null, true) }] as VersionStrategy)
        expect:
        project.version.toString() == '1.2.3'
    }

    def 'infers using first strategy selector returns true for in order'() {
        Project project = ProjectBuilder.builder().build()
        ReleasePluginExtension extension = new ReleasePluginExtension(project)
        extension.grgit = GroovyMock(Grgit)
        extension.versionStrategy([
            getName: { 'b' },
            selector: { proj, grgit -> true },
            infer: { proj, grgit -> new ReleaseVersion('1.0.0', null, true) }] as VersionStrategy)
        extension.versionStrategy([
            getName: { 'a' },
            selector: { proj, grgit -> true },
            infer: { proj, grgit -> new ReleaseVersion('1.2.3', null, true) }] as VersionStrategy)
        expect:
        project.version.toString() == '1.0.0'
    }

    def 'infer uses default if it has default selector that passes when selector doesnt'() {
        given:
        Project project = ProjectBuilder.builder().build()
        ReleasePluginExtension extension = new ReleasePluginExtension(project)
        extension.grgit = GroovyMock(Grgit)
        extension.versionStrategy([
            getName: { 'b' },
            selector: { proj, grgit -> false },
            infer: { proj, grgit -> new ReleaseVersion('1.0.0', null, true) }] as VersionStrategy)
        extension.defaultVersionStrategy = [
            getName: { 'a' },
            selector: { proj, grgit -> false },
            defaultSelector: { proj, grgit -> true },
            infer: { proj, grgit -> new ReleaseVersion('1.2.3', null, true) }] as DefaultVersionStrategy
        expect:
        project.version.toString() == '1.2.3'
    }

    def 'infer fails if no strategy selected including the default strategy'() {
        given:
        Project project = ProjectBuilder.builder().build()
        ReleasePluginExtension extension = new ReleasePluginExtension(project)
        extension.grgit = GroovyMock(Grgit)
        extension.versionStrategy([
            getName: { 'b' },
            selector: { proj, grgit -> false },
            infer: { proj, grgit -> new ReleaseVersion('1.0.0', null, true) }] as VersionStrategy)
        extension.defaultVersionStrategy = [
            getName: { 'a' },
            selector: { proj, grgit -> false },
            infer: { proj, grgit -> new ReleaseVersion('1.2.3', null, true) }] as VersionStrategy
        when:
        project.version.toString()
        then:
        thrown(GradleException)
    }

    def 'infer fails if no strategy selected and no default set'() {
        Project project = ProjectBuilder.builder().build()
        ReleasePluginExtension extension = new ReleasePluginExtension(project)
        extension.grgit = GroovyMock(Grgit)
        extension.versionStrategy([
            getName: { 'b' },
            selector: { proj, grgit -> false },
            infer: { proj, grgit -> new ReleaseVersion('1.0.0', null, true) }] as VersionStrategy)
        extension.versionStrategy([
            getName: { 'a' },
            selector: { proj, grgit -> false },
            infer: { proj, grgit -> new ReleaseVersion('1.2.3', null, true) }] as VersionStrategy)
        when:
        project.version.toString()
        then:
        thrown(GradleException)
    }

    def 'infer of multiple sub-git-projects is done correctly'() {
        given:
        Project rootProject = ProjectBuilder.builder().withName("a").build()
        Project subRootA    = ProjectBuilder.builder().withName("a_1").withParent(rootProject).build()
        Project subProjectA = ProjectBuilder.builder().withName("a_2").withParent(rootProject).build()
        Project subProjectB = ProjectBuilder.builder().withName('a_3').withParent(rootProject).build()
        Project subProjectBA = ProjectBuilder.builder().withName('a_3_1').withParent(subProjectB).build()

        ReleasePluginExtension extensionRoot = new ReleasePluginExtension(rootProject)
        extensionRoot.grgit = GroovyMock(Grgit)
        extensionRoot.defaultVersionStrategy = [
            getName: { 'root' },
            selector: { proj, grgit -> true },
            infer: { proj, grgit -> new ReleaseVersion('1.1.1', null, true) }] as VersionStrategy

        ReleasePluginExtension extensionA = new ReleasePluginExtension(subProjectA)
        extensionA.grgit = GroovyMock(Grgit)
        extensionA.defaultVersionStrategy = [
            getName: { 'a' },
            selector: { proj, grgit -> true },
            infer: { proj, grgit -> new ReleaseVersion('2.2.2', null, true) }] as VersionStrategy

        ReleasePluginExtension extensionB = new ReleasePluginExtension(subProjectB)
        extensionB.grgit = GroovyMock(Grgit)
        extensionB.defaultVersionStrategy = [
            getName: { 'b' },
            selector: { proj, grgit -> true },
            infer: { proj, grgit -> new ReleaseVersion('3.3.3', null, true) }] as VersionStrategy

        expect:
        rootProject.version.toString() == '1.1.1'
        subRootA.version.toString() == '1.1.1'
        subProjectA.version.toString() == '2.2.2'
        subProjectB.version.toString() == '3.3.3'
        subProjectBA.version.toString() == '3.3.3'
    }
}
