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

import com.github.zafarkhaja.semver.Version

import org.ajoberstar.gradle.git.release.base.ReleaseVersion
import org.ajoberstar.gradle.git.release.semver.ChangeScope
import org.ajoberstar.gradle.git.release.semver.NearestVersion
import org.ajoberstar.gradle.git.release.semver.NearestVersionLocator
import org.ajoberstar.gradle.git.release.semver.SemVerStrategyState
import org.ajoberstar.gradle.git.release.semver.StrategyUtil
import org.ajoberstar.grgit.Branch
import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Status
import org.ajoberstar.grgit.service.BranchService

import org.gradle.api.GradleException
import org.gradle.api.Project

import spock.lang.Specification
import spock.lang.Unroll

class StrategiesSpec extends Specification {
    SemVerStrategyState initialState = new SemVerStrategyState([:])
    @Unroll('Normal.USE_SCOPE_PROP with scope #scope results in #version')
    def 'Normal.USE_SCOPE_PROP'() {
        def nearestVersion = new NearestVersion(normal: Version.valueOf('1.2.3'))
        def initialState = new SemVerStrategyState(
            scopeFromProp: scope,
            nearestVersion: nearestVersion)
        expect:
        Strategies.Normal.USE_SCOPE_PROP.infer(initialState) ==
            new SemVerStrategyState(
                scopeFromProp: scope,
                nearestVersion: nearestVersion,
                inferredNormal: version)
        where:
        scope | version
        ChangeScope.PATCH | '1.2.4'
        ChangeScope.MINOR | '1.3.0'
        ChangeScope.MAJOR | '2.0.0'
    }

    def 'Normal.USE_NEAREST_ANY with different nearest any and normal uses any\'s normal'() {
        given:
        def nearestVersion = new NearestVersion(
            any: Version.valueOf('1.2.5-beta.1'),
            normal: Version.valueOf('1.2.4'))
        def initialState = new SemVerStrategyState(nearestVersion: nearestVersion)
        expect:
        Strategies.Normal.USE_NEAREST_ANY.infer(initialState) ==
            new SemVerStrategyState(nearestVersion: nearestVersion, inferredNormal: '1.2.5')
    }


    def 'Normal.USE_NEAREST_ANY with same nearest any and normal does nothing'() {
        given:
        def initialState = new SemVerStrategyState(
            nearestVersion: new NearestVersion(
                any: Version.valueOf('1.2.4'),
                normal: Version.valueOf('1.2.4')))
        expect:
        Strategies.Normal.USE_NEAREST_ANY.infer(initialState) == initialState
    }

    def 'Normal.ENFORCE_BRANCH_MAJOR_X fails if nearest normal can\'t be incremented to something that complies with branch'() {
        given:
        def initialState = new SemVerStrategyState(
            nearestVersion: new NearestVersion(normal: Version.valueOf('1.2.3')),
            currentBranch: new Branch(fullName: 'refs/heads/3.x'))
        when:
        Strategies.Normal.ENFORCE_BRANCH_MAJOR_X.infer(initialState)
        then:
        thrown(GradleException)
        where:
        version << ['1.2.3', '3.1.0', '4.0.0']
    }

    def 'Normal.ENFORCE_BRANCH_MAJOR_X correctly increments version to comply with branch'() {
        given:
        def initialState = new SemVerStrategyState(
            nearestVersion: new NearestVersion(normal: Version.valueOf(nearest)),
            currentBranch: new Branch(fullName: 'refs/heads/3.x'))
        expect:
        Strategies.Normal.ENFORCE_BRANCH_MAJOR_X.infer(initialState) ==
            initialState.copyWith(inferredNormal: inferred)
        where:
        nearest | inferred
        '2.0.0' | '3.0.0'
        '2.3.0' | '3.0.0'
        '2.3.4' | '3.0.0'
        '3.0.0' | null
        '3.1.0' | null
        '3.1.2' | null
    }

    def 'Normal.ENFORCE_GITFLOW_BRANCH_MAJOR_X correctly increments version to comply with branch'() {
        given:
        def initialState = new SemVerStrategyState(
            nearestVersion: new NearestVersion(normal: Version.valueOf(nearest)),
            currentBranch: new Branch(fullName: "refs/heads/${branch}"))
        expect:
        Strategies.Normal.ENFORCE_GITFLOW_BRANCH_MAJOR_X.infer(initialState) ==
            initialState.copyWith(inferredNormal: inferred)
        where:
        branch        | nearest | inferred
        'release/3.x' | '2.0.0' | '3.0.0'
        'release-3.x' | '2.3.4' | '3.0.0'
        'release-3.x' | '3.0.0' | null
        'release/3.x' | '3.1.0' | null
    }

    def 'Normal.ENFORCE_BRANCH_MAJOR_MINOR_X fails if nearest normal can\'t be incremented to something that complies with branch'() {
        given:
        def initialState = new SemVerStrategyState(
            nearestVersion: new NearestVersion(normal: Version.valueOf(version)),
            currentBranch: new Branch(fullName: 'refs/heads/3.2.x'))
        when:
        Strategies.Normal.ENFORCE_BRANCH_MAJOR_MINOR_X.infer(initialState)
        then:
        thrown(GradleException)
        where:
        version << ['1.2.3', '3.0.0', '3.3.0', '4.2.0']
    }

    def 'Normal.ENFORCE_BRANCH_MAJOR_MINOR_X correctly increments version to comply with branch'() {
        given:
        def initialState = new SemVerStrategyState(
            nearestVersion: new NearestVersion(normal: Version.valueOf(nearest)),
            currentBranch: new Branch(fullName: "refs/heads/${branch}"))
        expect:
        Strategies.Normal.ENFORCE_BRANCH_MAJOR_MINOR_X.infer(initialState) ==
            initialState.copyWith(inferredNormal: inferred)
        where:
        branch  | nearest | inferred
        '3.0.x' | '2.0.0' | '3.0.0'
        '3.0.x' | '2.1.0' | '3.0.0'
        '3.0.x' | '2.1.2' | '3.0.0'
        '3.0.x' | '3.0.0' | '3.0.1'
        '3.0.x' | '3.0.1' | '3.0.2'
        '3.2.x' | '3.1.0' | '3.2.0'
        '3.2.x' | '3.1.2' | '3.2.0'
        '3.2.x' | '3.2.0' | '3.2.1'
        '3.2.x' | '3.2.1' | '3.2.2'
    }

    def 'Normal.ENFORCE_GITFLOW_BRANCH_MAJOR_MINOR_X correctly increments version to comply with branch'() {
        given:
        def initialState = new SemVerStrategyState(
            nearestVersion: new NearestVersion(normal: Version.valueOf(nearest)),
            currentBranch: new Branch(fullName: "refs/heads/${branch}"))
        expect:
        Strategies.Normal.ENFORCE_GITFLOW_BRANCH_MAJOR_MINOR_X.infer(initialState) ==
            initialState.copyWith(inferredNormal: inferred)
        where:
        branch          | nearest | inferred
        'release/3.0.x' | '2.0.0' | '3.0.0'
        'release-3.0.x' | '3.0.0' | '3.0.1'
        'release-3.2.x' | '3.1.2' | '3.2.0'
        'release/3.2.x' | '3.2.1' | '3.2.2'
    }

    def 'Normal.ENFORCE_BRANCH_MAJOR_MINOR_X correctly increments version to comply with branch if normalStrategy is to increment minor instead of patch'() {
        given:
        def initialState = new SemVerStrategyState(
            nearestVersion: new NearestVersion(normal: Version.valueOf(nearest)),
            currentBranch: new Branch(fullName: "refs/heads/${branch}"))
        def strategy = StrategyUtil.one(Strategies.Normal.ENFORCE_BRANCH_MAJOR_MINOR_X, Strategies.Normal.useScope(ChangeScope.MINOR))
        expect:
        strategy.infer(initialState) == initialState.copyWith(inferredNormal: inferred)
        where:
        branch  | nearest | inferred
        '3.0.x' | '2.0.0' | '3.0.0'
        '3.0.x' | '2.1.0' | '3.0.0'
        '3.0.x' | '2.1.2' | '3.0.0'
        '3.0.x' | '3.0.0' | '3.0.1'
        '3.0.x' | '3.0.1' | '3.0.2'
        '3.2.x' | '3.1.0' | '3.2.0'
        '3.2.x' | '3.1.2' | '3.2.0'
        '3.2.x' | '3.2.0' | '3.2.1'
        '3.2.x' | '3.2.1' | '3.2.2'
    }

    def 'Normal.ENFORCE_GITFLOW_BRANCH_MAJOR_MINOR_X correctly increments version to comply with branch if normalStrategy is to increment minor instead of patch'() {
        given:
        def initialState = new SemVerStrategyState(
            nearestVersion: new NearestVersion(normal: Version.valueOf(nearest)),
            currentBranch: new Branch(fullName: "refs/heads/${branch}"))
        def strategy = StrategyUtil.one(Strategies.Normal.ENFORCE_GITFLOW_BRANCH_MAJOR_MINOR_X, Strategies.Normal.useScope(ChangeScope.MINOR))
        expect:
        strategy.infer(initialState) == initialState.copyWith(inferredNormal: inferred)
        where:
        branch          | nearest | inferred
        'release/3.0.x' | '2.0.0' | '3.0.0'
        'release-3.0.x' | '3.0.0' | '3.0.1'
        'release-3.2.x' | '3.1.2' | '3.2.0'
        'release/3.2.x' | '3.2.1' | '3.2.2'
    }

    def 'Normal.useScope correctly increments normal'() {
        given:
        def initialState = new SemVerStrategyState(
            nearestVersion: new NearestVersion(normal: Version.valueOf('1.2.3')))
        expect:
        Strategies.Normal.useScope(scope).infer(initialState) == initialState.copyWith(inferredNormal: inferred)
        where:
        scope             | inferred
        ChangeScope.PATCH | '1.2.4'
        ChangeScope.MINOR | '1.3.0'
        ChangeScope.MAJOR | '2.0.0'
    }

    def 'PreRelease.NONE does nothing'() {
        expect:
        Strategies.PreRelease.NONE.infer(new SemVerStrategyState([:])) == new SemVerStrategyState([:])
    }

    def 'PreRelease.STAGE_FIXED always replaces inferredPreRelease with stageFromProp'() {
        given:
        def initialState = new SemVerStrategyState(
            stageFromProp: 'boom',
            inferredPreRelease: initialPreRelease)
        expect:
        Strategies.PreRelease.STAGE_FIXED.infer(initialState) == initialState.copyWith(inferredPreRelease: 'boom')
        where:
        initialPreRelease << [null, 'other']
    }

    def 'PreRelease.STAGE_FLOAT will append the stageFromProp to the nearest any\'s pre release, if any, unless it has higher precedence'() {
        given:
        def initialState = new SemVerStrategyState(
            stageFromProp: 'boom',
            inferredNormal: '1.1.0',
            inferredPreRelease: 'other',
            nearestVersion: new NearestVersion(
                normal: Version.valueOf('1.0.0'),
                any: Version.valueOf(nearest)))
        expect:
        Strategies.PreRelease.STAGE_FLOAT.infer(initialState) == initialState.copyWith(inferredPreRelease: expected)
        where:
        nearest                    | expected
        '1.0.0'                    | 'boom'
        '1.0.1-cat.something.else' | 'boom'
        '1.1.0-and.1'              | 'boom'
        '1.1.0-cat.1'              | 'cat.1.boom'
        '1.1.0-cat.something.else' | 'cat.something.else.boom'
    }

    def 'PreRelease.COUNT_INCREMENTED will increment the nearest any\'s pre release or set to 1 if not found'() {
        given:
        def initialState = new SemVerStrategyState(
            inferredNormal: '1.1.0',
            inferredPreRelease: initialPreRelease,
            nearestVersion: new NearestVersion(
                normal: Version.valueOf('1.0.0'),
                any: Version.valueOf(nearestAny)))
        expect:
        Strategies.PreRelease.COUNT_INCREMENTED.infer(initialState) == initialState.copyWith(inferredPreRelease: expected)
        where:
        nearestAny           | initialPreRelease | expected
        '1.0.0'              | null              | '1'
        '1.0.0'              | 'other'           | 'other.1'
        '1.0.1-beta.1'       | 'other'           | 'other.1'
        '2.0.0-beta.1'       | 'other'           | 'other.1'
        '1.1.0-beta.1'       | 'other'           | 'other.1'
        '1.1.0-beta.1'       | 'beta'            | 'beta.2'
        '1.1.0-beta.99'      | 'beta'            | 'beta.100'
        '1.1.0-beta'         | 'beta'            | 'beta.1'
        '1.1.0-beta.1'       | 'beta.1.alpha'    | 'beta.1.alpha.1'
        '1.1.0-beta.1'       | 'beta.1.alpha'    | 'beta.1.alpha.1'
        '1.1.0-beta.2.alpha' | 'beta'            | 'beta.3'
    }

    def 'PreRelease.COUNT_COMMITS_SINCE_ANY will append distanceFromAny'() {
        given:
        def initialState = new SemVerStrategyState(
            inferredPreRelease: initialPreRelease,
            nearestVersion: new NearestVersion(distanceFromAny: distance))
        expect:
        Strategies.PreRelease.COUNT_COMMITS_SINCE_ANY.infer(initialState) == initialState.copyWith(inferredPreRelease: expected)
        where:
        initialPreRelease | distance | expected
        null              | 0        | '0'
        null              | 54       | '54'
        'other'           | 0        | 'other.0'
        'other'           | 54       | 'other.54'
    }

    def 'PreRelease.SHOW_UNCOMMITTED appends uncommitted only if repo is dirty'() {
        given:
        def initialState = new SemVerStrategyState(
            inferredPreRelease: initialPreRelease,
            repoDirty: dirty)
        expect:
        Strategies.PreRelease.SHOW_UNCOMMITTED.infer(initialState) == initialState.copyWith(inferredPreRelease: expected)
        where:
        initialPreRelease | dirty | expected
        null              | false | null
        null              | true  | 'uncommitted'
        'other'           | false | 'other'
        'other'           | true  | 'other.uncommitted'
    }

    def 'BuildMetadata.NONE does nothing'() {
        expect:
        Strategies.BuildMetadata.NONE.infer(new SemVerStrategyState([:])) == new SemVerStrategyState([:])
    }

    def 'BuildMetadata.COMMIT_ABBREVIATED_ID uses current HEAD\'s abbreviated id'() {
        given:
        def initialState = new SemVerStrategyState(currentHead: new Commit(id: '5e9b2a1e98b5670a90a9ed382a35f0d706d5736c'))
        expect:
        Strategies.BuildMetadata.COMMIT_ABBREVIATED_ID.infer(initialState) ==
            initialState.copyWith(inferredBuildMetadata: '5e9b2a1')
    }

    def 'BuildMetadata.COMMIT_FULL_ID uses current HEAD\'s abbreviated id'() {
        given:
        def id = '5e9b2a1e98b5670a90a9ed382a35f0d706d5736c'
        def initialState = new SemVerStrategyState(currentHead: new Commit(id: id))
        expect:
        Strategies.BuildMetadata.COMMIT_FULL_ID.infer(initialState) ==
            initialState.copyWith(inferredBuildMetadata: id)
    }

    def 'BuildMetadata.TIMESTAMP uses current time'() {
        expect:
        def newState = Strategies.BuildMetadata.TIMESTAMP.infer(new SemVerStrategyState([:]))
        def metadata = newState.inferredBuildMetadata
        metadata ==~ /\d{4}\.\d{2}\.\d{2}\.\d{2}\.\d{2}\.\d{2}/
        newState == new SemVerStrategyState(inferredBuildMetadata: metadata)
    }

    def 'SNAPSHOT works as expected'() {
        given:
        def project = mockProject(scope, stage)
        def grgit = mockGrgit(repoDirty)
        def locator = mockLocator(nearestNormal, nearestAny)
        expect:
        Strategies.SNAPSHOT.doInfer(project, grgit, locator) == new ReleaseVersion(expected, nearestNormal, false)
        where:
        scope   | stage      | nearestNormal | nearestAny   | repoDirty | expected
        null    | null       | '1.0.0'       | '1.0.0'      | false     | '1.0.1-SNAPSHOT'
        null    | null       | '1.0.0'       | '1.0.0'      | true      | '1.0.1-SNAPSHOT'
        null    | 'SNAPSHOT' | '1.0.0'       | '1.1.0-beta' | true      | '1.1.0-SNAPSHOT'
        null    | 'SNAPSHOT' | '1.0.0'       | '1.1.0-zed'  | true      | '1.1.0-SNAPSHOT'
        'PATCH' | 'SNAPSHOT' | '1.0.0'       | '1.1.0-zed'  | true      | '1.0.1-SNAPSHOT'
        'MINOR' | 'SNAPSHOT' | '1.0.0'       | '1.1.0-zed'  | true      | '1.1.0-SNAPSHOT'
        'MAJOR' | 'SNAPSHOT' | '1.0.0'       | '1.1.0-zed'  | true      | '2.0.0-SNAPSHOT'
    }

    def 'DEVELOPMENT works as expected'() {
        given:
        def project = mockProject(scope, stage)
        def grgit = mockGrgit(repoDirty)
        def locator = mockLocator(nearestNormal, nearestAny)
        expect:
        Strategies.DEVELOPMENT.doInfer(project, grgit, locator) == new ReleaseVersion(expected, nearestNormal, false)
        where:
        scope   | stage      | nearestNormal | nearestAny      | repoDirty | expected
        null    | null       | '1.0.0'       | '1.0.0'         | false     | '1.0.1-dev.2+5e9b2a1'
        null    | null       | '1.0.0'       | '1.0.0'         | true      | '1.0.1-dev.2.uncommitted+5e9b2a1'
        null    | null       | '1.0.0'       | '1.1.0-alpha.1' | false     | '1.1.0-dev.2+5e9b2a1'
        null    | null       | '1.0.0'       | '1.1.0-rc.3'    | false     | '1.1.0-rc.3.dev.2+5e9b2a1'
        null    | null       | '1.0.0'       | '1.1.0-rc.3'    | true      | '1.1.0-rc.3.dev.2.uncommitted+5e9b2a1'
        'PATCH' | 'dev'      | '1.0.0'       | '1.0.0'         | false     | '1.0.1-dev.2+5e9b2a1'
        'MINOR' | 'dev'      | '1.0.0'       | '1.0.0'         | false     | '1.1.0-dev.2+5e9b2a1'
        'MAJOR' | 'dev'      | '1.0.0'       | '1.0.0'         | false     | '2.0.0-dev.2+5e9b2a1'
    }

    def 'PRE_RELEASE works as expected'() {
        def project = mockProject(scope, stage)
        def grgit = mockGrgit(repoDirty)
        def locator = mockLocator(nearestNormal, nearestAny)
        expect:
        Strategies.PRE_RELEASE.doInfer(project, grgit, locator) == new ReleaseVersion(expected, nearestNormal, true)
        where:
        scope   | stage       | nearestNormal | nearestAny          | repoDirty | expected
        null    | null        | '1.0.0'       | '1.0.0'             | false     | '1.0.1-milestone.1'
        null    | 'milestone' | '1.0.0'       | '1.0.0'             | false     | '1.0.1-milestone.1'
        null    | 'rc'        | '1.0.0'       | '1.0.0'             | false     | '1.0.1-rc.1'
        'PATCH' | 'milestone' | '1.0.0'       | '1.0.0'             | false     | '1.0.1-milestone.1'
        'MINOR' | 'milestone' | '1.0.0'       | '1.0.0'             | false     | '1.1.0-milestone.1'
        'MAJOR' | 'milestone' | '1.0.0'       | '1.0.0'             | false     | '2.0.0-milestone.1'
        null    | 'rc'        | '1.0.0'       | '1.1.0-milestone.1' | false     | '1.1.0-rc.1'
        null    | 'milestone' | '1.0.0'       | '1.1.0-milestone.1' | false     | '1.1.0-milestone.2'
        null    | 'rc'        | '1.0.0'       | '1.1.0-rc'          | false     | '1.1.0-rc.1'
        null    | 'rc'        | '1.0.0'       | '1.1.0-rc.4.dev.1'  | false     | '1.1.0-rc.5'
    }

    def 'Strategies.FINAL works as expected'() {
        def project = mockProject(scope, stage)
        def grgit = mockGrgit(repoDirty)
        def locator = mockLocator(nearestNormal, nearestAny)
        expect:
        Strategies.FINAL.doInfer(project, grgit, locator) == new ReleaseVersion(expected, nearestNormal, true)
        where:
        scope   | stage       | nearestNormal | nearestAny          | repoDirty | expected
        null    | null        | '1.0.0'       | '1.0.0'             | false     | '1.0.1'
        'PATCH' | null        | '1.0.0'       | '1.0.0'             | false     | '1.0.1'
        'MINOR' | null        | '1.0.0'       | '1.0.0'             | false     | '1.1.0'
        'MAJOR' | null        | '1.0.0'       | '1.0.0'             | false     | '2.0.0'
        'MAJOR' | 'final'     | '1.0.0'       | '1.0.0'             | false     | '2.0.0'
        'MINOR' | 'final'     | '1.0.0'       | '1.1.0-alpha.2'     | false     | '1.1.0'
    }

    def 'PRE_RELEASE_ALPHA_BETA works as expected'() {
        def project = mockProject(scope, stage)
        def grgit = mockGrgit(repoDirty)
        def locator = mockLocator(nearestNormal, nearestAny)
        expect:
        Strategies.PRE_RELEASE_ALPHA_BETA.doInfer(project, grgit, locator) == new ReleaseVersion(expected, nearestNormal, true)
        where:
        scope   | stage       | nearestNormal | nearestAny          | repoDirty | expected
        null    | null        | '1.0.0'       | '1.0.0'             | false     | '1.0.1-alpha.1'
        null    | 'alpha'     | '1.0.0'       | '1.0.0'             | false     | '1.0.1-alpha.1'
        null    | 'beta'      | '1.0.0'       | '1.0.0'             | false     | '1.0.1-beta.1'
        null    | 'rc'        | '1.0.0'       | '1.0.0'             | false     | '1.0.1-rc.1'
        'PATCH' | 'alpha'     | '1.0.0'       | '1.0.0'             | false     | '1.0.1-alpha.1'
        'MINOR' | 'alpha'     | '1.0.0'       | '1.0.0'             | false     | '1.1.0-alpha.1'
        'MAJOR' | 'alpha'     | '1.0.0'       | '1.0.0'             | false     | '2.0.0-alpha.1'
        'PATCH' | 'beta'      | '1.0.0'       | '1.0.0'             | false     | '1.0.1-beta.1'
        'MINOR' | 'beta'      | '1.0.0'       | '1.0.0'             | false     | '1.1.0-beta.1'
        'MAJOR' | 'beta'      | '1.0.0'       | '1.0.0'             | false     | '2.0.0-beta.1'
        null    | 'rc'        | '1.0.0'       | '1.1.0-beta.1'      | false     | '1.1.0-rc.1'
        null    | 'beta'      | '1.0.0'       | '1.1.0-beta.1'      | false     | '1.1.0-beta.2'
        null    | 'rc'        | '1.0.0'       | '1.1.0-rc'          | false     | '1.1.0-rc.1'
        null    | 'rc'        | '1.0.0'       | '1.1.0-rc.4.dev.1'  | false     | '1.1.0-rc.5'
    }

    def mockProject(String scope, String stage) {
        Project project = Mock()

        project.hasProperty('release.scope') >> (scope as boolean)
        project.property('release.scope') >> scope

        project.hasProperty('release.stage') >> (stage as boolean)
        project.property('release.stage') >> stage

        return project
    }

    def mockGrgit(boolean repoDirty, String branchName = 'master') {
        Grgit grgit = GroovyMock()

        Status status = Mock()
        status.clean >> !repoDirty
        grgit.status() >> status

        grgit.head() >> new Commit(id: '5e9b2a1e98b5670a90a9ed382a35f0d706d5736c')

        BranchService branch = GroovyMock()
        branch.current >> new Branch(fullName: "refs/heads/${branchName}")
        grgit.branch >> branch

        return grgit
    }

    def mockLocator(String nearestNormal, String nearestAny) {
        NearestVersionLocator locator = Mock()
        locator.locate(_) >> new NearestVersion(
            normal: Version.valueOf(nearestNormal),
            distanceFromNormal: 5,
            any: Version.valueOf(nearestAny),
            distanceFromAny: 2
        )
        return locator
    }
}
