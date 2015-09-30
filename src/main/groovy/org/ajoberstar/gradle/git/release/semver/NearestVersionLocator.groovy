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
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Tag
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Locates the nearest {@link org.ajoberstar.grgit.Tag tag}s whose names can be
 * parsed as a {@link com.github.zafarkhaja.semver.Version version}. Both the
 * absolute nearest version tag and the nearest "normal version" tag are
 * included.
 *
 * <p>
 *   Primarily used as part of version inference to determine the previous
 *   version.
 * </p>
 *
 * @since 0.8.0
 */
class NearestVersionLocator {
	private static final Logger logger = LoggerFactory.getLogger(NearestVersionLocator)
	private static final Version UNKNOWN = Version.valueOf('0.0.0')

	/**
	 * Locate the nearest version in the given repository
	 * starting from the current HEAD.
	 *
	 * <p>
	 * All tag names are parsed to determine if they are valid
	 * version strings. Tag names can begin with "v" (which will
	 * be stripped off).
	 * </p>
	 *
	 * <p>
	 * The nearest tag is determined by getting a commit log between
	 * the tag and {@code HEAD}. The version tag with the smallest
	 * log from a pure count of commits will have its version returned. If two
	 * version tags have a log of the same size, the versions will be compared
	 * to find the one with the highest precedence according to semver rules.
	 * For example, {@code 1.0.0} has higher precedence than {@code 1.0.0-rc.2}.
	 * For tags with logs of the same size and versions of the same precedence
	 * it is undefined which will be returned.
	 * </p>
	 *
	 * <p>
	 * Two versions will be returned: the "any" version and the "normal" version.
	 * "Any" is the absolute nearest tagged version. "Normal" is the nearest
	 * tagged version that does not include a pre-release segment.
	 * </p>
	 *
	 * @param grgit the repository to locate the tag in
	 * @param fromRevStr the revision to consider current.
	 * Defaults to {@code HEAD}.
	 * @return the version corresponding to the nearest tag
	 */
	NearestVersion locate(Grgit grgit) {
		logger.debug('Locate beginning on branch: {}', grgit.branch.current.fullName)

		List<Tag> tags = grgit.tag.list()
		Map<ObjectId, List<Tag>> tagsByCommit = tags.groupBy { ObjectId.fromString(it.commit.id) }
		Map<ObjectId, List<Version>> versionsByCommit = tagsByCommit.collectEntries { key, value ->
			List<Version> versions = value.collect { tag ->
				Version version = TagUtil.parseAsVersion(tag)
				logger.debug('Tag {} ({}) parsed as {} version.', tag.name, tag.commit.abbreviatedId, version)
				version
			}.findAll { it }
			Collections.sort(versions, Collections.reverseOrder())
			[key, versions]
		} as Map<ObjectId, List<Version>>;

		logger.debug('Versions by commit {}.', versionsByCommit)

		Version anyVersion = UNKNOWN
		Version normalVersion = UNKNOWN
		int distanceFromAny = 0
		int distanceFromNormal = 0

		Git jgit = grgit.repository.jgit
		Iterator<RevCommit> log = jgit.log().call().iterator()
		int distance = 0
		while (log.hasNext() && !versionsByCommit.isEmpty()) {
			ObjectId revCommitId = log.next().id
			List<Version> versions = versionsByCommit.get(revCommitId)
			if (versions) {
				for (Version version : versions) {
					if (anyVersion == UNKNOWN) {
						logger.debug('Found any version {}, distance {}.', version, distance)
						anyVersion = version
						distanceFromAny = distance
					}
					if (normalVersion == UNKNOWN && version.preReleaseVersion.empty) {
						logger.debug('Found normal version {}, distance {}.', version, distance)
						normalVersion = version
						distanceFromNormal = distance
						break
					}
				}
				versionsByCommit.remove(revCommitId)
			}
			distance++
		}

		if (anyVersion == UNKNOWN) {
			distanceFromAny = distance
		}
		if (normalVersion == UNKNOWN) {
			distanceFromNormal = distance
		}

		logger.debug('Walked {} commit(s). Nearest release: {}, nearest any: {}.', distance, normalVersion, anyVersion)

		return new NearestVersion(anyVersion, normalVersion, distanceFromAny, distanceFromNormal)
	}
}
