package org.ajoberstar.gradle.git.release

import org.ajoberstar.gradle.git.semver.InferredVersion
import org.ajoberstar.gradle.util.ObjectUtil
import org.ajoberstar.grgit.Grgit

import com.github.zafarkhaja.semver.Version

import org.gradle.api.Project

class GrgitReleasePluginExtension {
	private final Project project
	Grgit grgit

	InferredVersion version = new InferredVersion()

	Closure tagReleaseIf = { version -> version.preReleaseVersion.empty }
	Closure createTagName = { version -> "v${version}" }

	Closure branchReleaseIf = { version ->
		version.preReleaseVersion.empty &&
			version.patchVersion == 0 &&
			version.minorVersion == 0
	}
	Closure createBranchName = { version -> "release-${version}" }


	GrgitReleasePluginExtension(Project project) {
		this.project = project
		this.grgit = Grgit.open(project.rootProject.file('.'))
	}

	void version(Closure closure) {

	}

	String getTagName(Version version) {
		return tagRelease(version) ? tagName(version) : null
	}

	String getBranchName(Version version) {
		return branchRelease(version) ? branchName(version) : null
	}
}
