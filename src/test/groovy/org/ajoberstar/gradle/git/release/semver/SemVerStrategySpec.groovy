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
package org.ajoberstar.gradle.git.release.semver

import com.github.zafarkhaja.semver.Version

import org.ajoberstar.gradle.git.release.base.ReleaseVersion
import org.ajoberstar.grgit.Branch
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Status
import org.ajoberstar.grgit.service.BranchService

import org.gradle.api.GradleException
import org.gradle.api.Project

import spock.lang.Specification

class SemVerStrategySpec extends Specification {
	Project project = GroovyMock()
	Grgit grgit = GroovyMock()

	def 'selector returns false if stage is not set to valid value'() {
		given:
		def strategy = new SemVerStrategy(stages: ['one', 'two'] as SortedSet)
		mockStage(stageProp)
		expect:
		!strategy.selector(project, grgit)
		where:
		stageProp << [null, 'test']
	}


	def 'selector returns false if repo is dirty and not allowed to be'() {
		given:
		def strategy = new SemVerStrategy(stages: ['one'] as SortedSet, allowDirtyRepo: false)
		mockStage('one')
		mockRepoClean(false)
		expect:
		!strategy.selector(project, grgit)
	}

	def 'selector returns true if repo is dirty and allowed and other criteria met'() {
		given:
		def strategy = new SemVerStrategy(stages: ['one'] as SortedSet, allowDirtyRepo: true)
		mockStage('one')
		mockRepoClean(false)
		mockBranchService()
		expect:
		strategy.selector(project, grgit)
	}

	def 'selector returns true if all criteria met'() {
		given:
		def strategy = new SemVerStrategy(stages: ['one', 'and'] as SortedSet, allowDirtyRepo: false)
		mockStage('one')
		mockRepoClean(true)
		mockBranchService()
		expect:
		strategy.selector(project, grgit)
	}

	def 'default selector returns false if stage is defined but not set to valid value'() {
		given:
		def strategy = new SemVerStrategy(stages: ['one', 'two'] as SortedSet)
		mockStage('test')
		expect:
		!strategy.defaultSelector(project, grgit)
	}

	def 'default selector returns true if stage is not defined'() {
		given:
		def strategy = new SemVerStrategy(stages: ['one', 'two'] as SortedSet)
		mockStage(null)
		mockRepoClean(true)
		expect:
		strategy.defaultSelector(project, grgit)
	}

	def 'default selector returns false if repo is dirty and not allowed to be'() {
		given:
		def strategy = new SemVerStrategy(stages: ['one'] as SortedSet, allowDirtyRepo: false)
		mockStage(stageProp)
		mockRepoClean(false)
		expect:
		!strategy.defaultSelector(project, grgit)
		where:
		stageProp << [null, 'one']
	}

	def 'default  selector returns true if repo is dirty and allowed and other criteria met'() {
		given:
		def strategy = new SemVerStrategy(stages: ['one'] as SortedSet, allowDirtyRepo: true)
		mockStage('one')
		mockRepoClean(false)
		mockBranchService()
		expect:
		strategy.defaultSelector(project, grgit)
	}

	def 'default selector returns true if all criteria met'() {
		given:
		def strategy = new SemVerStrategy(stages: ['one', 'and'] as SortedSet, allowDirtyRepo: false)
		mockStage('one')
		mockRepoClean(true)
		mockBranchService()
		expect:
		strategy.defaultSelector(project, grgit)
	}

	def 'infer returns correct version'() {
		given:
		mockScope(scope)
		mockStage(stage)
		mockRepoClean(false)
		mockBranchService()
		def nearest = new NearestVersion(
			normal: Version.valueOf('1.2.2'),
			any: Version.valueOf(nearestAny))
		def locator = mockLocator(nearest)
		def strategy = mockStrategy(scope, stage, nearest, createTag, enforcePrecedence)
		expect:
		strategy.doInfer(project, grgit, locator) == new ReleaseVersion('1.2.3-beta.1+abc123', '1.2.2', createTag)
		where:
		scope   | stage | nearestAny | createTag | enforcePrecedence
		'patch' | 'one' | '1.2.3'    | true      | false
		'minor' | 'one' | '1.2.2'    | true      | true
		'major' | 'one' | '1.2.2'    | false     | true
		'patch' | null  | '1.2.2'    | false     | true
	}

	def 'infer fails if stage is not listed in stages property'() {
		given:
		mockStage('other')
		def strategy = new SemVerStrategy(stages: ['one'] as SortedSet)
		when:
		strategy.doInfer(project, grgit, null)
		then:
		thrown(GradleException)
	}

	def 'infer fails if precedence enforced and violated'() {
		given:
		mockRepoClean(false)
		mockBranchService()
		def nearest = new NearestVersion(any: Version.valueOf('1.2.3'))
		def locator = mockLocator(nearest)
		def strategy = mockStrategy(null, 'and', nearest, false, true)
		when:
		strategy.doInfer(project, grgit, locator)
		then:
		thrown(GradleException)
	}

	private def mockScope(String scopeProp) {
		(0..1) * project.hasProperty('release.scope') >> (scopeProp as boolean)
		(0..1) * project.property('release.scope') >> scopeProp
	}

	private def mockStage(String stageProp) {
		(0..1) * project.hasProperty('release.stage') >> (stageProp as boolean)
		(0..1) * project.property('release.stage') >> stageProp
	}

	private def mockRepoClean(boolean isClean) {
		Status status = GroovyMock()
		(0..2) * status.clean >> isClean
		(0..2) * grgit.status() >> status
		0 * status._
	}

	private def mockBranchService() {
		BranchService branchService = GroovyMock()
		(0..1) * branchService.current >> new Branch(fullName: 'refs/heads/master')
		(0..2) * grgit.getBranch() >> branchService
		0 * branchService._
	}

	private def mockLocator(NearestVersion nearest) {
		NearestVersionLocator locator = Mock()
		locator.locate(grgit) >> nearest
		return locator
	}

	private def mockStrategy(String scope, String stage, NearestVersion nearest, boolean createTag, boolean enforcePrecedence) {
		PartialSemVerStrategy normal = Mock()
		PartialSemVerStrategy preRelease = Mock()
		PartialSemVerStrategy buildMetadata = Mock()
		SemVerStrategyState initial = new SemVerStrategyState([
			scopeFromProp: scope?.toUpperCase(),
			stageFromProp: stage ?: 'and',
			currentHead: null,
			currentBranch: new Branch(fullName: 'refs/heads/master'),
			repoDirty: true,
			nearestVersion: nearest])
		SemVerStrategyState afterNormal = initial.copyWith(inferredNormal: '1.2.3')
		SemVerStrategyState afterPreRelease = afterNormal.copyWith(inferredPreRelease: 'beta.1')
		SemVerStrategyState afterBuildMetadata = afterPreRelease.copyWith(inferredBuildMetadata: 'abc123')

		1 * normal.infer(initial) >> afterNormal
		1 * preRelease.infer(afterNormal) >> afterPreRelease
		1 * buildMetadata.infer(afterPreRelease) >> afterBuildMetadata
		0 * normal._
		0 * preRelease._
		0 * buildMetadata._

		return new SemVerStrategy(
			stages: ['one', 'and'] as SortedSet,
			normalStrategy: normal,
			preReleaseStrategy: preRelease,
			buildMetadataStrategy: buildMetadata,
			createTag: createTag,
			enforcePrecedence: enforcePrecedence
		)
	}
}
