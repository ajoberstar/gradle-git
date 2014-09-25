/*
 * Copyright 2012-2014 the original author or authors.
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

import groovy.transform.Immutable

import com.github.zafarkhaja.semver.Version

import org.ajoberstar.gradle.git.release.base.ReleaseVersion
import org.ajoberstar.gradle.git.release.base.VersionStrategy
import org.ajoberstar.grgit.Grgit

import org.gradle.api.GradleException
import org.gradle.api.Project

@Immutable(copyWith=true)
final class SemVerStrategy implements VersionStrategy {
	static final String SCOPE_PROP = 'release.scope'
	static final String STAGE_PROP = 'release.stage'

	String name
	SortedSet<String> stages
	boolean allowDirtyRepo
	boolean allowBranchBehind

	PartialSemVerStrategy normalStrategy
	PartialSemVerStrategy preReleaseStrategy
	PartialSemVerStrategy buildMetadataStrategy

	boolean createTag
	boolean enforcePrecedence

	@Override
	boolean selector(Project project, Grgit grgit) {
		String stage = getPropertyOrNull(project, STAGE_PROP)
		if (!stages.contains(stage)) {
			logger.info('Skipping {} strategy because stage ({}) is not one of: {}', name, stage, stages)
			return false
		} else if (!allowDirtyRepo && !grgit.status().clean) {
			logger.info('Skipping {} strategy because repo is dirty.', name)
			return false
		} else if (!allowBranchBehind && grgit.branch.status(branch: grgit.branch.current.fullName).behindCount > 0) {
			logger.info('Skipping {} strategy because current branch is behind tracked branch.', name)
			return false
		} else {
			return true
		}
	}

	@Override
	ReleaseVersion infer(Project project, Grgit grgit) {
		ChangeScope scope = getPropertyOrNull(project, SCOPE_PROP).with { scope ->
			scope == null ? null : ChangeScope.valueOf(scope.toUpperCase())
		}
		String stage = getPropertyOrNull(project, STAGE_PROP) ?: stages.first()
		logger.info('Beginning version inference using {} strategy and input scope ({}) and stage ({})', name, scope, stage)

		NearestVersion nearestVersion = NearestVersionLocator.locate(grgit)
		logger.debug('Located nearest version: {}', nearestVersion)

		SemVerStrategyState state = new SemVerStrategyState(
			scopeFromProp: scope,
			stageFromProp: stage,
			currentHead: grgit.head(),
			currentBranch: grgit.branch(),
			repoDirty: !grgit.status().clean,
			nearestVersion: nearestVersion
		)

		Version version = StrategyUtil.all(
			normalStrategy, preReleaseStrategy, buildMetadataStrategy).infer(state).toVersion()

		logger.warn('Inferred version: {}', version)

		if (enforcePrecedence && version < nearestVersion.any) {
			throw new GradleException("Inferred version (${version}) cannot be lower than nearest (${nearestVersion.any}). Required by selected strategy.")
		}

		return new ReleaseVersion(version, createTag)
	}

	private String getPropertyOrNull(Project project, String name) {
		return project.hasProperty(name) ? project[name] : null
	}
}
