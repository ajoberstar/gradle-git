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

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Plugin providing opinionated release functionality for Git projects.
 * @since 0.8.0
 */
class GrgitReleasePlugin implements Plugin<Project> {
	private static final Logger logger = LoggerFactory.getLogger(GrgitReleasePlugin)

	void apply(Project project) {
		GrgitReleasePluginExtension extension = project.extensions.create('release', GrgitReleasePluginExtension)
		project.version = extension.version
		addReadyVersionTaskRule(project, extension)
		addReleaseTaskRule(project, extension)
		addFallBackInferLogic(project, extension)
	}

	private void addReadyVersionTaskRule(Project project, GrgitReleasePluginExtension extension) {
		project.tasks.addRule('Pattern: ready<scope>VersionAs<stage>') { String taskName ->
			def m = taskName =~ /ready(.+)VersionAs(.+)/
			if (m) {
				project.tasks.create(taskName) {
					description = 'Ensures the project is ready to be released.'
					doLast {
						logger.info('Checking for uncommitted changes in repo.')
						ext.grgit = extension.grgit
						ext.status = grgit.status()
						if (!status.clean) {
							println "Repository has uncommitted changes:"
							(status.staged.allChanges + status.unstaged.allChanges).each { change ->
								println "\t${change}"
							}
							throw new IllegalStateException("Repository has uncommitted changes.")
						}

						logger.info('Fetching changes from remote.')
						grgit.fetch(remote: extension.remote)

						logger.info('Verifying current branch is not behind remote.')
						ext.branchStatus = grgit.branch.status(branch: grgit.branch.current.fullName)
						if (branchStatus.behindCount > 0) {
							println "Current branch is behind by ${branchStatus.behindCount} commits. Cannot proceed."
							throw new IllegalStateException("Current branch is behind ${extension.remote}.")
						}

						extension.version.infer(m[0][1].toLowerCase(), m[0][2].toLowerCase())
						logger.warn('Inferred version as {}', extension.version)

						if (!extension.version.releasable) {
							throw new IllegalStateException("No changes since ${extension.version}. There is nothing to release.")
						}
					}
				}
				project.tasks.all { task ->
					if (name != taskName) {
						task.mustRunAfter taskName
					}
				}
			}
		}
	}

	private void addReleaseTaskRule(Project project, GrgitReleasePluginExtension extension) {
		project.tasks.addRule('Pattern: release<scope>VersionAs<stage>') { String taskName ->
			def m = taskName =~ /release(.+)VersionAs(.+)/
			if (m) {
				project.tasks.create(taskName) {
					description = 'Releases version of this project.'
					dependsOn "ready${m[0][1]}VersionAs${m[0][2]}", extension.releaseTasks
					doLast {
						ext.grgit = extension.grgit
						ext.toPush = [grgit.branch.current.fullName]

						ext.tagName = extension.tagName
						if (tagName) {
							logger.warn('Tagging repository as {}', tagName)
							grgit.tag.add(name: tagName, message: "Release of ${extension.version}")
							toPush << tagName
						}

						logger.warn('Pushing changes in {} to {}', toPush, extension.remote)
						grgit.push(remote: extension.remote, refsOrSpecs: toPush)
					}
				}
			}
		}
	}

	private void addFallBackInferLogic(Project project, GrgitReleasePluginExtension extension) {
		project.gradle.taskGraph.whenReady { graph ->
			def inferTask = graph.allTasks.find { task ->
				task.name =~ /ready(.+)VersionAs(.+)/
			}
			if (!inferTask) {
				// the first untagged stage should be the lowest precedence given semver rules
				def stage = extension.version.untaggedStages.find()
				def scope = 'patch'
				logger.info('No ready<scope>VersionAs<stage> task requested. Inferring as {} {} release.', stage, scope)
				extension.version.infer(scope, stage)
				logger.warn('Inferred verison as {}', extension.version)
			}
		}
	}
}
