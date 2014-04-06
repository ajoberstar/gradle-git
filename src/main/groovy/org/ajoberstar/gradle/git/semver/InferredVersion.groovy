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
package org.ajoberstar.gradle.git.semver

import org.ajoberstar.grgit.Grgit
import com.github.zafarkhaja.semver.Version

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class InferredVersion {
	private static final Logger logger = LoggerFactory.getLogger(InferredVersion)
	private Version inferredVersion = null

	Grgit grgit
	SortedSet<String> untaggedStages = ['dev'] as SortedSet
	SortedSet<String> taggedStages = ['milestone', 'rc'] as SortedSet


	Closure<Boolean> useBuildMetadataForStage = { stage -> stage != 'final' }
	Closure<String> createBuildMetadata = { grgit.head().id }

	void infer(String scope, String stage) {
		infer(ChangeScope.valueOf(scope.toUpperCase()), stage)
	}

	void infer(ChangeScope scope, String stage) {
		if (scope == null) {
			throw new IllegalArgumentException('Scope cannot be null.')
		} else if (!getAllStages().contains(stage)) {
			throw new IllegalArgumentException('Invalid stage (${stage}). Must use one of: ${getAllStages()}')
		}
		logger.debug('Beginning version inference for {} version of {} change', stage, scope)

		NearestVersion nearest = NearestVersionLocator.locate(grgit)
		logger.debug('Located nearest version: {}', nearest)
		Version target = inferNormal(nearest.normal, scope)
		logger.debug('Inferred target normal version: {}', target)
		if (stage == 'final') {
			// do nothing
		} else if (untaggedStages.contains(stage)) {
			// use commit count
			target = target.setPreReleaseVersion("${stage}.${nearest.distance}")
		} else if (nearest.any.normalVersion == target.normalVersion && nearest.stage == stage) {
			// increment pre-release
			target = nearest.any.incrementPreReleaseVersion()
		} else {
			// first version for stage
			target = target.setPreReleaseVersion("${stage}.1")
		}

		if (useBuildMetadataForStage(stage)) {
			target = target.setBuildMetadata(createBuildMetadata())
		}
		logger.info('Inferred version {} for {} {} change.', target, stage, scope)
		inferredVersion = target
	}

	private Version inferNormal(Version previous, ChangeScope scope) {
		switch (scope) {
			case ChangeScope.MAJOR:
				return previous.incrementMajorVersion()
			case ChangeScope.MINOR:
				return previous.incrementMinorVersion()
			case ChangeScope.PATCH:
				return previous.incrementPatchVersion()
			default:
				throw new IllegalArgumentException("Invalid scope: ${scope}")
		}
	}

	SortedSet<String> getAllStages() {
		return untaggedStages + taggedStages + ['final']
	}

	@Override
	String toString() {
		if (inferredVersion) {
			return inferredVersion
		} else {
			throw new IllegalStateException("Version has not been inferred.")
		}
	}

	static enum ChangeScope {
		MAJOR,
		MINOR,
		PATCH
	}
}
