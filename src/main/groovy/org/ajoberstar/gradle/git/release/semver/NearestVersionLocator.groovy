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
import org.ajoberstar.gradle.git.release.base.TagStrategy
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Tag
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.ObjectId
import org.eclipse.jgit.revwalk.RevCommit
import org.eclipse.jgit.revwalk.RevWalk
import org.eclipse.jgit.revwalk.RevWalkUtils
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

	final TagStrategy strategy

	NearestVersionLocator(TagStrategy strategy) {
		this.strategy = strategy
	}

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

		// Reuse a single walk to make use of caching.
		RevWalk walk = new RevWalk(grgit.repository.jgit.repo)
		try {
			walk.retainBody = false

			def toRev = { obj ->
				def commit = grgit.resolve.toCommit(obj)
				def id = ObjectId.fromString(commit.id)
				walk.parseCommit(id)
			}

			List tags = grgit.tag.list().collect { tag ->
				[version: strategy.parseTag(tag), tag: tag, rev: toRev(tag)]
			}.findAll {
				it.version
			}

			List normalTags = tags.findAll { !it.version.preReleaseVersion }
			RevCommit head = toRev(grgit.head())

			def normal = findNearestVersion(walk, head, normalTags)
			def any = findNearestVersion(walk, head, tags)

			logger.debug('Nearest release: {}, nearest any: {}.', normal, any)
			return new NearestVersion(any.version, normal.version, any.distance, normal.distance)
		} finally {
			walk.close()
		}
	}

	private Map findNearestVersion(RevWalk walk, RevCommit head, List versionTags) {
		walk.reset()
		walk.markStart(head)
		Map versionTagsByRev = versionTags.groupBy { it.rev }

		def reachableVersionTags = walk.collectMany { rev ->
			def matches = versionTagsByRev[rev]
			if (matches) {
				// Parents can't be "nearer". Exclude them to avoid extra walking.
				rev.parents.each { walk.markUninteresting(it) }
			}
			matches ?: []
		}.each { versionTag ->
			versionTag.distance = RevWalkUtils.count(walk, head, versionTag.rev)
		}

		if (reachableVersionTags) {
			return reachableVersionTags.min { a, b ->
				def distanceCompare = a.distance <=> b.distance
				def versionCompare = (a.version <=> b.version) * -1
				distanceCompare == 0 ? versionCompare : distanceCompare
			}
		} else {
			return [version: UNKNOWN, distance: RevWalkUtils.count(walk, head, null)]
		}
	}
}
