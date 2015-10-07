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

import com.github.zafarkhaja.semver.Version

import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Locates the max {@link org.ajoberstar.grgit.Tag tag}s whose names can be
 * parsed as a {@link com.github.zafarkhaja.semver.Version version}. Both the
 * absolute max version tag and the max "normal version" tag are
 * included.
 *
 * <p>
 *   Primarily used as part of version inference to determine the next
 *   version.
 * </p>
 *
 * @since x.y.z
 */
class MaxVersionLocator {
	private static final Logger logger = LoggerFactory.getLogger(NearestVersionLocator)

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
	MaxVersion locate(Grgit grgit) {

		List versionTags = grgit.tag.list().inject([]) { list, tag ->
			Version version = TagUtil.parseAsVersion(tag)
			logger.debug('Tag {} ({}) parsed as {} version.', tag.name, tag.commit.abbreviatedId, version)
			if (version) {
				list << version
			}
			list
		}

		def normal = versionTags.findAll { versionTag ->
			versionTag.preReleaseVersion.empty
		}.min { a, b ->
			if (a > b) {
				return -1
			} else {
				return 1
			}
		}

		def any = versionTags.min { a, b ->
			if (a > b) {
				return -1
			} else {
				return 1
			}
		}

		Version anyVersion = any ? any : Version.valueOf('0.0.0')
		Version normalVersion = normal ? normal : Version.valueOf('0.0.0')

		return new MaxVersion(anyVersion, normalVersion)
	}
}
