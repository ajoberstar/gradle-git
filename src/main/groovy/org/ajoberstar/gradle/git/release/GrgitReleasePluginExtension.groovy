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
package org.ajoberstar.gradle.git.release

import com.github.zafarkhaja.semver.Version

import org.ajoberstar.gradle.git.semver.InferredVersion
import org.ajoberstar.grgit.Grgit

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
