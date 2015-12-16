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
package org.ajoberstar.gradle.git.release.base

import com.github.zafarkhaja.semver.ParseException
import com.github.zafarkhaja.semver.Version
import org.ajoberstar.grgit.Tag

/**
 * Handles the parsing of a {@link Tag} to a {@link Version} and converting the {@link ReleaseVersion} to a compatible
 * {@link Tag} string representation.
 */
interface TagHandler {
	/**
	 * Convert a {@link ReleaseVersion version} to a string representation to be used as a tag name.
	 * @param version The version that is the basis of the tag name
	 * @return A string tag name
     */
	String toTagString(ReleaseVersion version)

	/**
	 * Parse the {@link Tag tag} to a {@link Version version}
	 * @param tag the {@link Tag tag} to parse.
	 * @return the parsed {@link Version version} or {@code null} if the tag does not correspond to a version.
     */
	Version parseTag(Tag tag)

	static class Handlers {
		/**
		 * Default {@link TagHandler} implementation that adheres to the SemVer guidelines.
		 */
		static TagHandler semver(boolean prefixWithV) {
			new TagHandler() {
				@Override
				String toTagString(ReleaseVersion version) {
					prefixWithV ? "v${version.version}" : version.version
				}

				@Override
				Version parseTag(Tag tag) {
					try {
						Version.valueOf(tag.name[0] == 'v' ? tag.name[1..-1] : tag.name)
					} catch (ParseException e) {
						null
					}
				}
			}
		}
	}

}
