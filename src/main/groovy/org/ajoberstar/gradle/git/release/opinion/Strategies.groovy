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
package org.ajoberstar.gradle.git.release.opinion

import static org.ajoberstar.gradle.git.release.semver.StrategyUtil.*

import java.util.regex.Pattern

import org.ajoberstar.gradle.git.release.semver.ChangeScope
import org.ajoberstar.gradle.git.release.semver.PartialSemVerStrategy
import org.ajoberstar.gradle.git.release.semver.SemVerStrategy
import org.ajoberstar.gradle.git.release.semver.SemVerStrategyState

import org.gradle.api.GradleException

final class Strategies {
	static final class Normal {
		static final PartialSemVerStrategy USE_SCOPE_PROP = closure { state ->
			return incrementNormalFromScope(state, state.scopeFromProp)
		}

		static final PartialSemVerStrategy USE_NEAREST_ANY = closure { state ->
			def nearest = state.nearestVersion
			if (nearest.any == nearest.normal) {
				return state
			} else {
				return state.copyWith(inferredNormal: nearest.any.normalVersion)
			}
		}

		static final PartialSemVerStrategy ENFORCE_BRANCH_MAJOR_X = fromBranchPattern(~/^(\d+)\.x$/)

		static final PartialSemVerStrategy ENFORCE_BRANCH_MAJOR_MINOR_X = fromBranchPattern(~/^(\d+)\.(\d+)\.x$/)

		static PartialSemVerStrategy fromBranchPattern(Pattern pattern) {
			return closure { state ->
				def m = state.currentBranch.name =~ pattern
				if (m) {
					def major = m.groupCount() >= 1 ? parseIntOrZero(m[0][1]) : -1
					def minor = m.groupCount() >= 2 ? parseIntOrZero(m[0][2]) : -1

					def normal = state.nearestVersion.normal
					def majorDiff = major - normal.majorVersion
					def minorDiff = minor - normal.minorVersion

					if (minor <= 0 && majorDiff == 1) {
						return state.copyWith(inferredNormal: "${major}.0.0")
					} else if (minor > 0 && minorDiff == 1) {
						return state.copyWith(inferredNormal: "${major}.${minor}.0")
					} else if (majorDiff == 0 && (minor < 0 || minorDiff == 0)) {
						return state
					} else {
						throw new GradleException("Invalid branch (${state.currentBranch.name}) for nearest normal (${normal}).")
					}
				} else {
					return state
				}
			}
		}

		static PartialSemVerStrategy useScope(ChangeScope scope) {
			return closure { state -> incrementNormalFromScope(state, scope) }
		}
	}

	static final class PreRelease {
		static final PartialSemVerStrategy NONE = closure { state -> state }

		static final PartialSemVerStrategy STAGE_FIXED = closure { state -> state.copyWith(inferredPreRelease: state.stageFromProp)}

		static final PartialSemVerStrategy STAGE_FLOAT = closure { state ->
			def nearestPreRelease = state.nearestVersion.any.preReleaseVersion
			if (nearestPreRelease != null && nearestPreRelease > state.stageFromProp) {
				state.copyWith(inferredPreRelease: "${nearestPreRelease}.${state.stageFromProp}")
			} else {
				state.copyWith(inferredPreRelease: state.stageFromProp)
			}
		}

		static final PartialSemVerStrategy COUNT_INCREMENTED = closure { state ->
			def nearest = state.nearestVersion
			def currentPreIdents = state.inferredPreRelease ? state.inferredPreRelease.split('\\.') as List : []
			if (nearest.any == nearest.normal || nearest.any.normalVersion != state.inferredNormal) {
				currentPreIdents << '1'
			} else {
				def nearestPreIdents = nearest.any.preReleaseVersion.split('\\.')
				if (nearestPreIdents.size() <= currentPreIdents.size()) {
					currentPreIdents << '1'
				} else if (currentPreIdents == nearestPreIdents[0..(currentPreIdents.size() - 1)]) {
					def count = parseIntOrZero(nearestPreIdents[currentPreIdents.size()])
					if (count == 0 || nearestPreIdents.size() == currentPreIdents.size() + 1) {
						currentPreIdents << Integer.toString(count + 1)
					} else {
						currentPreIdents << Integer.toString(count)
					}
				} else {
					currentPreIdents << '1'
				}
			}
			return state.copyWith(inferredPreRelease: currentPreIdents.join('.'))
		}

		static final PartialSemVerStrategy COUNT_COMMITS_SINCE_ANY = closure { state ->
			def count = state.nearestVersion.distanceFromAny
			def inferred = state.inferredPreRelease ? "${state.inferredPreRelease}.${count}" : "${count}"
			return state.copyWith(inferredPreRelease: inferred)
		}

		static final PartialSemVerStrategy SHOW_UNCOMMITTED = closure { state ->
			if (state.repoDirty) {
				def inferred = state.inferredPreRelease ? "${state.inferredPreRelease}.uncommitted" : 'uncommitted'
				state.copyWith(inferredPreRelease: inferred)
			} else {
				state
			}
		}
	}

	static final class BuildMetadata {
		static final PartialSemVerStrategy NONE = closure { state -> state }
		static final PartialSemVerStrategy COMMIT_ABBREVIATED_ID = closure { state -> state.copyWith(inferredBuildMetadata: state.currentHead.abbreviatedId) }
		static final PartialSemVerStrategy COMMIT_FULL_ID = closure { state -> state.copyWith(inferredBuildMetadata: state.currentHead.id) }
		static final PartialSemVerStrategy TIMESTAMP = closure { state -> state.copyWith(inferredBuildMetadata: new Date().format('yyyy.MM.dd.hh.mm.ss')) }
	}

	static final SemVerStrategy DEFAULT = new SemVerStrategy(
		name: '',
		stages: [],
		allowDirtyRepo: false,
		allowBranchBehind: false,
		normalStrategy: one(Normal.USE_SCOPE_PROP, Normal.USE_NEAREST_ANY, Normal.useScope(ChangeScope.PATCH)),
		preReleaseStrategy: PreRelease.NONE,
		buildMetadataStrategy: BuildMetadata.NONE,
		createTag: true,
		enforcePrecedence: true
	)

	static final SemVerStrategy SNAPSHOT = DEFAULT.copyWith(
		name: 'snapshot',
		stages: ['SNAPSHOT'],
		allowDirtyRepo: true,
		allowBranchBehind: true,
		preReleaseStrategy: PreRelease.STAGE_FIXED,
		createTag: false,
		enforcePrecedence: false
	)

	static final SemVerStrategy DEVELOPMENT = DEFAULT.copyWith(
		name: 'development',
		stages: ['dev'],
		allowDirtyRepo: true,
		allowBranchBehind: true,
		preReleaseStrategy: all(PreRelease.STAGE_FLOAT, PreRelease.COUNT_COMMITS_SINCE_ANY, PreRelease.SHOW_UNCOMMITTED),
		buildMetadataStrategy: BuildMetadata.COMMIT_ABBREVIATED_ID,
		createTag: false
	)

	static final SemVerStrategy PRE_RELEASE = DEFAULT.copyWith(
		name: 'pre-release',
		stages: ['milestone', 'rc'],
		preReleaseStrategy: all(PreRelease.STAGE_FIXED, PreRelease.COUNT_INCREMENTED),
		buildMetadataStrategy: BuildMetadata.COMMIT_ABBREVIATED_ID
	)

	static final SemVerStrategy FINAL = DEFAULT.copyWith(
		name: 'final',
		stages: ['final'],
		preReleaseStrategy: PreRelease.NONE,
		buildMetadataStrategy: BuildMetadata.NONE
	)
}
