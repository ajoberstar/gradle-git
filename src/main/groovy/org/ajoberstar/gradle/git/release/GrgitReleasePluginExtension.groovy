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
import org.ajoberstar.grgit.util.ConfigureUtil

import org.gradle.api.Project

class GrgitReleasePluginExtension {
	InferredVersion version = new InferredVersion()
	Grgit grgit

	String remote = 'origin'

	boolean prefixTagNameWithV = true

	Closure branchReleaseIf = { version ->
		version.preReleaseVersion.empty &&
			version.patchVersion == 0
	}
	Closure determineBranchNameFor = { version -> "release-${version.majorVersion}.${version.minorVersion}" }

	Iterable releaseTasks = []

	void version(Closure closure) {
		ConfigureUtil.configure(version, closure)
	}

	void setGrgit(Grgit grgit) {
		this.grgit = grgit
		version.grgit = grgit
	}

	String getTagName() {
		if ((version.taggedStages + ['final']).contains(version.stage)) {
			return prefixTagNameWithV ? "v${version}" : version
		} else {
			return null
		}
	}

	String getBranchName() {
		return branchReleaseIf(version.inferredVersion) ? determineBranchNameFor(version.inferredVersion) : null
	}
}
