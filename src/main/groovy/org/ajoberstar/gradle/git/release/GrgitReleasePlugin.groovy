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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPlugin

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Plugin providing opinionated release functionality for Git projects.
 * @since 0.8.0
 */
class GrgitReleasePlugin implements Plugin<Project> {
	private static final Logger logger = LoggerFactory.getLogger(GrgitReleasePlugin)
	private static final RELEASE_TASK_NAME = 'release'
	private static final PREPARE_TASK_NAME = 'prepare'

	void apply(Project project) {
		GrgitReleasePluginExtension extension = project.extensions.create('release', GrgitReleasePluginExtension, project)
		project.version = extension.version
		addValidateSinceTagsTask(project, extension)
		addPrepareTask(project, extension)
		addReleaseTask(project, extension)
	}

	private void addValidateSinceTagsTask(Project project, GrgitReleasePluginExtension extension) {
		project.tasks.create('validateSinceTags') {
			onlyIf { extension.enforceSinceTags }
			ext.source = []
			project.plugins.withType(JavaPlugin) {
				source = project.sourceSets.main.allJava
			}
			doLast {
				def tags = extension.grgit.tag.list()
				def anyInvalid = false
				source.each { file ->
					file.readLines().eachWithIndex { line, index ->
						def m = line =~ /@since\s+(\S+)/
						if (m) {
							def sinceVersion = m[0][1]
							def target = extension.version.inferredVersion.toString()
							def targetNormal = extension.version.inferredVersion.normalVersion.toString()
							def anyTags = tags.any { tag -> [sinceVersion, "v${sinceVersion}".toString()].contains(tag.name) }
							if ([target, targetNormal].contains(sinceVersion) || anyTags) {
								logger.debug('Valid @since tag {} on line {} of file {}.', sinceVersion, index + 1, file)
							} else {
								logger.error('Inalid @since tag {} on line {} of file {}.', sinceVersion, index + 1, file)
								anyInvalid = true
							}
						}
					}
				}
				if (anyInvalid) {
					throw new IllegalStateException("One or more files have invalid @since tags. See output above.")
				}
			}

		}
	}

	private void addPrepareTask(Project project, GrgitReleasePluginExtension extension) {
		project.tasks.create(PREPARE_TASK_NAME) {
			description = 'Ensures the project is ready to be released.'
			doLast {
				logger.info('Checking for uncommitted changes in repo.')
				ext.grgit = extension.grgit
				ext.status = grgit.status()
				if (!status.clean) {
					println 'Repository has uncommitted changes:'
					(status.staged.allChanges + status.unstaged.allChanges).each { change ->
						println "\t${change}"
					}
					throw new IllegalStateException('Repository has uncommitted changes.')
				}

				logger.info('Fetching changes from remote.')
				grgit.fetch(remote: extension.remote)

				logger.info('Verifying current branch is not behind remote.')
				ext.branchStatus = grgit.branch.status(branch: grgit.branch.current.fullName)
				if (branchStatus.behindCount > 0) {
					println "Current branch is behind by ${branchStatus.behindCount} commits. Cannot proceed."
					throw new IllegalStateException("Current branch is behind ${extension.remote}.")
				}

				if (!extension.version.releasable) {
					throw new IllegalStateException("No changes since ${extension.version}. There is nothing to release.")
				}
			}
		}

		project.tasks.all { task ->
			if (name == PREPARE_TASK_NAME) {
				task.finalizedBy 'validateSinceTags'
			} else {
				task.mustRunAfter PREPARE_TASK_NAME
			}
		}
	}

	private void addReleaseTask(Project project, GrgitReleasePluginExtension extension) {
		project.tasks.create('release') {
			description = 'Releases this project.'
			dependsOn PREPARE_TASK_NAME
			dependsOn { extension.releaseTasks }
			doLast {
				ext.grgit = extension.grgit
				ext.toPush = [grgit.branch.current.fullName]

				ext.tagName = extension.tagName
				if (tagName) {
					logger.warn('Tagging repository as {}', tagName)
					grgit.tag.add(name: tagName, message: extension.tagMessage)
					toPush << tagName
				}

				logger.warn('Pushing changes in {} to {}', toPush, extension.remote)
				grgit.push(remote: extension.remote, refsOrSpecs: toPush)
			}
		}
	}
}
