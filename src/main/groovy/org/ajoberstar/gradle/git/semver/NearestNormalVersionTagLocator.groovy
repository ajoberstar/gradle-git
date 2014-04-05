package org.ajoberstar.gradle.git.semver

import com.github.zafarkhaja.semver.Version
import com.github.zafarkhaja.semver.MetadataVersion

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
		return version.preReleaseVersion == MetadataVersion.NULL
	}
}
