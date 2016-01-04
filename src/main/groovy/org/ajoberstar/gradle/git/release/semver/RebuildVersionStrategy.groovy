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
package org.ajoberstar.gradle.git.release.semver

import org.ajoberstar.gradle.git.release.base.ReleasePluginExtension
import org.ajoberstar.gradle.git.release.base.ReleaseVersion
import org.ajoberstar.gradle.git.release.base.VersionStrategy
import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit

import org.gradle.api.Project

/**
 * Strategy that infers the version based on the tag on the current
 * HEAD.
 */
class RebuildVersionStrategy implements VersionStrategy {
	public static final RebuildVersionStrategy INSTANCE = new RebuildVersionStrategy()

	private RebuildVersionStrategy() {
		// just hiding the constructor
	}

	@Override
	String getName() {
		return 'rebuild'
	}

	/**
	 * Determines whether this strategy should be used to infer the version.
	 * <ul>
	 * <li>Return {@code false}, if any project properties starting with "release." are set.</li>
	 * <li>Return {@code false}, if there aren't any tags on the current HEAD that can be parsed as a version.</li>
	 * <li>Return {@code true}, otherwise.</li>
	 * </ul>
	 */
	@Override
	boolean selector(Project project, Grgit grgit) {
		return grgit.status().clean &&
			project.properties.keySet().find { it.startsWith('release.') } == null &&
			getHeadVersion(project, grgit)
	}

	/**
	 * Infers the version based on the version tag on the current HEAD with the
	 * highest precendence.
	 */
	@Override
	ReleaseVersion infer(Project project, Grgit grgit) {
		String version = getHeadVersion(project, grgit)
		return new ReleaseVersion(version, version, false)
	}

	private String getHeadVersion(Project project, Grgit grgit) {
		def tagStrategy = project.extensions.getByType(ReleasePluginExtension).tagStrategy
		Commit head = grgit.head()
		return grgit.tag.list().findAll {
			it.commit == head
		}.collect {
			tagStrategy.parseTag(it)
		}.findAll {
			it != null
		}.max()?.toString()
	}
}
