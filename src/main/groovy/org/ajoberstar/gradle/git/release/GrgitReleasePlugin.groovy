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

class GrgitReleasePlugin implements Plugin<Project> {
	void apply(Project project) {
		GrgitReleasePluginExtension extension = project.extensions.create('release', GrgitReleasePluginExtension)
		project.version = extension.version
		addReadyVersionTaskRule(project, extension)
		addReleaseTaskRule(project, extension)
	}

	private void addReadyVersionTaskRule(Project project, GrgitReleasePluginExtension extension) {
		project.tasks.addRule('Pattern: ready<scope>VersionAs<stage>') { String taskName ->
			def m = taskName =~ /ready(.+)VersionAs(.+)/
			if (m) {
				project.tasks.create(taskName) {
					description = 'Ensures the project is ready to be released.'
					doLast {
						extension.version.infer(m[0][1], m[0][2])
						println "Inferred ${extension.version}"

						ext.grgit = extension.grgit
						ext.status = grgit.status()
						if (!status.clean) {
							println "Repository has uncommitted changes: ${status}"
							throw new IllegalStateException("Repository has uncommitted changes.")
						}

						grgit.fetch(remote: extension.remote)

						ext.branchStatus = grgit.branch.status(branch: grgit.branch.current.fullName)
						if (branchStatus.behindCount > 0) {
							println "Current branch is behind by ${branchStatus.behindCount} commits. Cannot proceed."
							throw new IllegalStateException("Current branch is behind ${extension.remote}.")
						}
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

	private void addReleaseTaskRule(Project project, GrgitReleasePluginExtension extension) {
		project.tasks.addRule('Pattern: release<scope>VersionAs<stage>') { String taskName ->
			def m = taskName =~ /release(.+)VersionAs(.+)/
			if (m) {
				project.tasks.create(taskName) {
					description = 'Releases version of this project.'
					dependsOn "ready${m[0][1]}VersionAs${m[0][2]}", extension.releaseTasks
					doLast {
						ext.toPush = [grgit.branch.current.fullName]

						ext.tagName = extension.tagName
						if (tagName) {
							grgit.tag.add(name: tagName, message: "Release of ${extension.version}")
							toPush << tagName
						}

						ext.branchName = extension.branchName
						if (branchName) {
							grgit.branch.add(name: branchName)
							toPush << branchName
						}

						grgit.push(remote: extension.remote, refsOrSpecs: toPush)
					}
				}
			}
		}
	}
}
