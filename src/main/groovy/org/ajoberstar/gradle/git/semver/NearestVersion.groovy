package org.ajoberstar.gradle.git.semver

import groovy.transform.Canonical
import com.github.zafarkhaja.semver.Version

/**
 * Nearest version tags reachable from the current HEAD. The version 0.0.0
 * will be returned for any
 * @since 0.8.0
 */
@Canonical
class NearestVersion {
	/**
	 * The nearest version that is tagged.
	 */
	final Version any

	/**
	 * The nearest normal (i.e. non-prerelease) version that is tagged.
	 */
	final Version normal

	/**
	 * The number of commits since {@code normal} reachable from HEAD.
	 */
	final int distance

	/**
	 * The pre-release stage of the {@code any} version.
	 * @return the value to the left of the first dot (if any) in the {@code any}
	 * version's preRelease segment
	 */
	String getStage() {
		return any.preReleaseVersion.split('\\.', 2)[0]
	}
}
