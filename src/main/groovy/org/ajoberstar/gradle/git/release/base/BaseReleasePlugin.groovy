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
package org.ajoberstar.gradle.git.release.base

import org.gradle.api.Plugin
import org.gradle.api.Project

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Plugin providing the base structure of gradle-git's flavor of release
 * behavior. The plugin can be applied using the {@code org.ajoberstar.release-base} id.
 *
 * <p>
 * The plugin adds the {@link ReleasePluginExtension} and a {@code release} task.
 * </p>
 *
 * @see org.ajoberstar.gradle.git.release.opinion.Strategies
 * @see org.ajoberstar.gradle.git.release.opinion.OpinionReleasePlugin
 * @see <a href="https://github.com/ajoberstar/gradle-git/wiki/org.ajoberstar.release-base">Wiki Doc</a>
 */
class BaseReleasePlugin implements Plugin<Project> {
	private static final Logger logger = LoggerFactory.getLogger(BaseReleasePlugin)
	private static final String RELEASE_TASK_NAME = 'release'

	void apply(Project project) {
		def extension = project.extensions.create('release', ReleasePluginExtension, project)
		addReleaseTask(project, extension)
	}

	private void addReleaseTask(Project project, ReleasePluginExtension extension) {
		project.tasks.create(RELEASE_TASK_NAME) {
			description = 'Releases this project.'
			doLast {
				// force version inference if it hasn't happened already
				project.version.toString()

				ext.grgit = extension.grgit
				ext.toPush = [grgit.branch.current.fullName]

				ext.tagName = extension.tagStrategy.maybeCreateTag(grgit, project.version.inferredVersion)
				if (tagName) {
					toPush << tagName
				}

				logger.warn('Pushing changes in {} to {}', toPush, extension.remote)
				grgit.push(remote: extension.remote, refsOrSpecs: toPush)
			}
		}
	}
}
