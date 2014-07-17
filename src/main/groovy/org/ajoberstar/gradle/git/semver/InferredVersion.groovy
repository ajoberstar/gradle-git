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

import com.github.zafarkhaja.semver.Version

import org.ajoberstar.grgit.Grgit

import org.gradle.api.Project

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Version object that infers its value from the existing Git tags in the
 * repository and the stage and scope of the change.
 *
 * <p>
 * Version will be inferred by finding the nearest tagged version using
 * {@link org.ajoberstar.gradle.git.semver.NearestVersionLocator}. Based on
 * the scope of the changes and the stage of their completion (both of which
 * must be provided to the {@link #infer(String, String)} method) the nearest
 * version will be incremented appropriately to result in the inferred version.
 * </p>
 *
 * <p>
 * If the closest tagged version is v1.1.0 (for the normal version)
 * and the closes absolute version is v1.2.0-milestone.1, here are some examples
 * of the versions that would be inferred (assuming the default settings on
 * this object):
 * </p>
 *
 * <table>
 *   <tr><td>Scope</td><td>Stage</td><td>Inferred Version</td></tr>
 *   <tr><td>patch</td><td>milestone</td><td>1.1.1-milestone.1</td></tr>
 *   <tr><td>minor</td><td>dev</td><td>1.2.0-dev.&lt;commits since 1.1.0&gt;</td></tr>
 *   <tr><td>minor</td><td>milestone</td><td>1.2.0-milestone.2</td></tr>
 *   <tr><td>minor</td><td>rc</td><td>1.2.0-rc.1</td></tr>
 *   <tr><td>minor</td><td>final</td><td>1.2.0</td></tr>
 *   <tr><td>major</td><td>milestone</td><td>2.0.0-milestone.1</td></tr>
 * </table>
 *
 * @since 0.8.0
 */
class InferredVersion {
	private static final Logger logger = LoggerFactory.getLogger(InferredVersion)
	private static final SCOPE_PROP_NAME = 'release.scope'
	private static final STAGE_PROP_NAME = 'release.stage'
    static final String SNAPSHOT_STAGE = 'SNAPSHOT'

	private final Project project
	private Version inferredVersion = null
	private ChangeScope scope
	private String stage
	private boolean releasable

	private NearestVersion nearest = null

	/**
	 * The git repository to infer from.
	 */
	Grgit grgit

	/**
	 * Valid stages for versions that should not have tags created for them.
	 * These stages will be numbered based on the number of commits since the
	 * nearest normal version rather than the number of releases for the
	 * inferred normal verison and stage. Defaults to ['dev'].
	 */
	SortedSet<String> untaggedStages = ['dev'] as SortedSet

	/**
	 * Valid stages for versions that should have tags created for them. These
	 * stages will be numbered in incrementing fashion based on the number of
	 * the nearest absolute version, if it has the same normal version and stage.
	 * Defaults to ['milestone', 'rc'].
	 */
	SortedSet<String> taggedStages = ['milestone', 'rc'] as SortedSet

	/**
	 * Closure to determine whether build metadata should be included in the
	 * inferred version. Should accept a single String argument for
	 * the stage. Should return true or false. Defaults to returning true for
	 * stages besides 'final' and false otherwise.
	 */
	Closure<Boolean> useBuildMetadataForStage = { stage -> stage != 'final' }

	/**
	 * Closure to create the build metadata to use in the inferred versions.
	 * Only exected for stages that meet {@link #useBuildMetadataForStage}.
	 * Will called with no arguments and should return a String representing
	 * the build metadata for this version.
	 */
	Closure<String> createBuildMetadata = { grgit.head().abbreviatedId }

	InferredVersion(Project project) {
		this.project = project
	}

	/**
	 * Infers the version using project properties {@code release.scope} and
	 * {@code release.stage} or falls back to a scope of {@code patch} or the
	 * lowest precedence stage from {@code untaggedStages}.
	 */
	void infer() {
		def stage
		if (project.hasProperty(STAGE_PROP_NAME)) {
			stage = project[STAGE_PROP_NAME]
		} else {
			// the first untagged stage should be the lowest precedence given semver rules
			stage = untaggedStages.find()
		}
		def scope = project.hasProperty(SCOPE_PROP_NAME) ? project[SCOPE_PROP_NAME] : 'patch'
		infer(scope, stage)
	}

	/**
	 * Infers the version using the given arguments.
	 * @param scope the scope of the change. Must be a string matching one of
	 * {@link #ChangeScope}'s values
	 * @param stage one of the valid stages for this object. See {@link #getAllStages()}
	 */
	void infer(String scope, String stage) {
		infer(ChangeScope.valueOf(scope.toUpperCase()), stage)
	}

	/**
	 * Infers the version using the given arguments.
	 * @param scope the scope of the change.
	 * @param stage one of the valid stages for this object. See {@link #getAllStages()}
	 */
	void infer(ChangeScope scope, String stage) {
		if (scope == null) {
			throw new IllegalArgumentException('Scope cannot be null.')
		} else if (!getAllStages().contains(stage)) {
			throw new IllegalArgumentException("Invalid stage (${stage}). Must use one of: ${getAllStages()}")
		}
		this.scope = scope
		this.stage = stage
		logger.info('Beginning version inference for {} version of {} change', stage, scope)

		this.nearest = NearestVersionLocator.locate(grgit)
		logger.debug('Located nearest version: {}', nearest)

		this.releasable = nearest.distanceFromAny > 0
		if (releasable) {
			Version target = inferNormal(nearest.normal, scope)
			logger.debug('Inferred target normal version: {}', target)
			if (stage == 'final') {
				// do nothing
			} else if (untaggedStages.contains(stage)) {
				target = determineUntaggedVersion(target, nearest)
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
			logger.warn('Inferred version {} for {} {} change.', target, stage, scope)
			inferredVersion = target
		} else {
			logger.warn('No committed changes since {}, using that version.', nearest.any)
			inferredVersion = nearest.any
		}
	}

    /**
     * Adds SNAPSHOT stage to the list of untagged versions.
     */
    void addSnapshotStage() {
        untaggedStages << SNAPSHOT_STAGE
    }

    /**
     * Determines untagged version.
     *
     * @param target Target version
     * @param nearestVersion Nearest version
     * @return Created version
     */
    private Version determineUntaggedVersion(Version target, NearestVersion nearestVersion) {
        if(stage == SNAPSHOT_STAGE) {
            return target.setPreReleaseVersion(SNAPSHOT_STAGE)
        }

        // use commit count
        target.setPreReleaseVersion("${stage}.${nearestVersion.distanceFromNormal}")
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

	Version getInferredVersion() {
		if (inferredVersion) {
			return inferredVersion
		} else {
			throw new IllegalStateException("Version has not been inferred.")
		}
	}

	ChangeScope getScope() {
		if (inferredVersion) {
			return scope
		} else {
			throw new IllegalStateException("Version has not been inferred.")
		}
	}

	String getStage() {
		if (inferredVersion) {
			return stage
		} else {
			throw new IllegalStateException("Version has not been inferred.")
		}
	}

	boolean getReleasable() {
		if (inferredVersion) {
			return releasable
		} else {
			throw new IllegalStateException("Version has not been inferred.")
		}
	}

	/**
	 * The nearest version that was found in the repository.
	 * @return the nearest version
	 */
	NearestVersion getNearest() {
		if (inferredVersion) {
			return nearest
		} else {
			throw new IllegalStateException("Version has not been inferred.")
		}
	}

	/**
	 * Gets all stages considered valid by this object. Includes all
	 * values from {@link #untaggedStages} and {@link taggedStages}, plus
	 * {@code final}.
	 * @return all valid stages to be used in {@link infer(String, String)}
	 */
	SortedSet<String> getAllStages() {
		return untaggedStages + taggedStages + ['final']
	}

	@Override
	String toString() {
		if (!inferredVersion) {
			logger.info('Version being inferred due to call to toString')
			infer()
		}
		return inferredVersion
	}

	static enum ChangeScope {
		MAJOR,
		MINOR,
		PATCH
	}
}
