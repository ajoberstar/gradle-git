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
import com.github.zafarkhaja.semver.GrammarException
import com.github.zafarkhaja.semver.UnexpectedElementTypeException
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Locates the nearest {@link org.ajoberstar.grgit.Tag tag} whose name can be
 * parsed as a {@link com.github.zafarkhaja.semver.Version version}. Any tag
 * that can be parsed as a version is considered by this locator. Subclasses may
 * provide different restrictions.
 *
 * <p>
 *   Primarily used as part of version inference to determine the previous
 *   version.
 * </p>
 *
 * @since 0.8.0
 */
abstract class NearestVersionTagLocator {
	private static final Logger logger = LoggerFactory.getLogger(NearestVersionTagLocator)

	/**
	 * Locate the nearest version tag in the given repository
	 * starting from the given rev string.
	 *
	 * <p>
	 * All tag names are parsed to determine if they are valid
	 * version strings. Tag names can begin with "v" (which will
	 * be stripped off). Only tags that pass {@link #acceptVersion(Version)}
	 * will be considered by the locator.
	 * </p>
	 *
	 * <p>
	 * The nearest tag is determined by getting a commit log between
	 * the tag and {@code fromRevStr}. The version tag with the smallest
	 * log from a pure count of commits will have its version returned. If two
	 * version tags have a log of the same size, it is undefined which will be
	 * returned.
	 * </p>
	 * @param grgit the repository to locate the tag in
	 * @param fromRevStr the revision to consider current.
	 * Defaults to {@code HEAD}.
	 * @return the version corresponding to the nearest tag
	 */
	Version locate(Grgit grgit, String fromRevStr = 'HEAD') {
		grgit.tag.list().inject([:]) { map, tag ->
			Version version = parseAsVersion(tag.name)
			if (version && acceptVersion(version)) {
				map[tag] = version
			}
			map
		}.max { tag, version ->
			grgit.log {
				range tag.fullName, fromRevStr
			}.size()
		}.with { entry ->
			entry.value
		}
	}

	/**
	 * Whether or not the parsed version should be accepted for this
	 * locator. This locator will accept all versions.
	 * @param version the version to test
	 * @return {@code true} if the version should be considered as part of
	 * the locator, {@code false} otherwise
	 */
	protected boolean acceptVersion(Version version) {
		return true
	}

	private Version parseAsVersion(String name) {
		try {
			return Version.valueOf(extractName(name))
		} catch (GrammarException e) {
			logger.error('Internal semver error.')
			throw e
		} catch (UnexpectedElementTypeException e) {
			logger.debug('Invalid version string: {}', name)
			return null
		}
	}

	private String extractName(String tagName) {
		if (tagName.charAt(0) == 'v') {
			return tagName[1..-1]
		} else {
			return tagName
		}
	}
}
