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

/**
 * Extension providing properties to configure the behavior of the GrgitReleasePlugin.
 * @since 0.8.0
 */
class GrgitReleasePluginExtension {
	/**
	 * The version class that will be set on the project by default. When the
	 * release is "readied" the intended version will be inferred from the
	 * state of the repository.
	 */
	InferredVersion version = new InferredVersion()

	/**
	 * The repository to perform the release on.
	 */
	Grgit grgit

	/**
	 * The remote to fetch from and push to. Defaults to {@code origin}.
	 */
	String remote = 'origin'

	/**
	 * Whether or not to prefix release tag names with a "v". Defaults to
	 * {@code true}.
	 */
	boolean prefixTagNameWithV = true

	/**
	 * Tasks that should be executed before the release is tagged, branched, and
	 * pushed to the remote. Defaults to an empty list.
	 */
	Iterable releaseTasks = []

	/**
	 * Configure the version. Delegates to {@link #version}.
	 * @param closure a closure to configure the version
	 */
	void version(Closure closure) {
		ConfigureUtil.configure(version, closure)
	}

	/**
	 * Sets the repository to use on both this extension and the underlying
	 * {@link #version}.
	 * @param grgit the repository to use
	 */
	void setGrgit(Grgit grgit) {
		this.grgit = grgit
		version.grgit = grgit
	}

	/**
	 * Gets the tag name that will be used.
	 * @return the release tag name
	 */
	String getTagName() {
		if ((version.taggedStages + ['final']).contains(version.stage)) {
			return prefixTagNameWithV ? "v${version}" : version
		} else {
			return null
		}
	}
}
