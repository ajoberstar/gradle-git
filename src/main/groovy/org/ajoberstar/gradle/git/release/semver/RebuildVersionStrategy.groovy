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
package org.ajoberstar.gradle.git.release.semver

import org.ajoberstar.gradle.git.release.base.ReleaseVersion
import org.ajoberstar.gradle.git.release.base.VersionStrategy
import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit

import org.gradle.api.Project

class RebuildVersionStrategy implements VersionStrategy {
	@Override
	String getName() {
		return 'rebuild'
	}

	@Override
	boolean selector(Project project, Grgit grgit) {
		return grgit.status().clean &&
			project.properties.keySet().find { it.startsWith('release.') } == null &&
			getHeadVersion(grgit)
	}

	@Override
	ReleaseVersion infer(Project project, Grgit grgit) {
		String version = getHeadVersion(grgit)
		return new ReleaseVersion(version, version, false)
	}

	private String getHeadVersion(Grgit grgit) {
		Commit head = grgit.head()
		return grgit.tag.list().findAll {
			it.commit == head
		}.collect {
			TagUtil.parseAsVersion(it)
		}.findAll {
			it != null
		}.max()?.toString()
	}
}
