package org.ajoberstar.gradle.git.semver

class InferredVersion {
	Grgit grgit
	ReleaseType releaseType
	String preReleaseType


	@Override
	String toString() {
		return version
	}

	static enum ReleaseType {
		MAJOR,
		MINOR,
		PATCH
	}
}
