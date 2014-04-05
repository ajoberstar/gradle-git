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

/**
 * Same as {@link NearestVersionTagLocator}, but only "release" versions
 * are considered by the locator.
 * @since 0.8.0
 */
class NearestNormalVersionTagLocator extends NearestVersionTagLocator {
	/**
	 * Whether or not the parsed version should be accepted for this
	 * locator. This locator will only accept versions without a pre-release
	 * element. Versions with build metadata will be accepted.
	 * @param version the version to test
	 * @return {@code true} if the version should be considered as part of
	 * the locator, {@code false} otherwise
	 */
	@Override
	protected boolean acceptVersion(Version version) {
		return version.preReleaseVersion.empty
	}
}
