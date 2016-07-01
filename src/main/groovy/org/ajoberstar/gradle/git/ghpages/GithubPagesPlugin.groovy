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
package org.ajoberstar.gradle.git.ghpages

import org.ajoberstar.grgit.Grgit
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy

/**
 * Plugin to enable publishing to gh-pages branch of Github.
 * @since 0.1.0
 */
class GithubPagesPlugin implements Plugin<Project> {
	static final String PREPARE_TASK_NAME = 'prepareGhPages'
	static final String PUBLISH_TASK_NAME = 'publishGhPages'

	/**
	 * Applies the plugin to the given project.
	 * @param project the project
	 */
	void apply(Project project) {
		GithubPagesPluginExtension extension = project.extensions.create('githubPages', GithubPagesPluginExtension, project)
		configureTasks(project, extension)
	}

	/**
	 * Configures the tasks to publish to gh-pages.
	 * @param project the project to configure
	 * @param extension the plugin extension
	 */
	private void configureTasks(final Project project, final GithubPagesPluginExtension extension) {
		Task prepare = createPrepareTask(project, extension)
		Task publish = createPublishTask(project, extension)
		publish.dependsOn(prepare)
	}

	private Task createPrepareTask(Project project, GithubPagesPluginExtension extension) {
		Task task = project.tasks.create(PREPARE_TASK_NAME, Copy)
		task.with {
			description = 'Prepare the gh-pages changes locally'
			with extension.pages.realSpec
			into { extension.workingDir }
			doFirst {
				extension.workingDir.deleteDir()
				ext.repo = Grgit.clone(
						uri: extension.repoUri,
						refToCheckout: extension.targetBranch,
						dir: extension.workingDir,
						credentials: extension.credentials?.toGrgit()
				)

				// check if on the correct branch, which implies it doesn't exist
				if (repo.branch.current.name != extension.targetBranch) {
					repo.checkout(branch: extension.targetBranch, orphan: true)
					// need to wipe out the current files
					extension.deleteExistingFiles = true
				}

				def targetDir = new File(extension.workingDir, extension.pages.relativeDestinationDir)
				def filesList = targetDir.list({ dir, name ->
					return !name.equals('.git')
				})
				if (filesList && extension.deleteExistingFiles) {
					repo.remove(patterns: filesList)
				}
			}
		}
		return task
	}

	private Task createPublishTask(Project project, GithubPagesPluginExtension extension) {
		return project.tasks.create(PUBLISH_TASK_NAME) {
			description = 'Publishes all gh-pages changes to Github'
			group = 'publishing'
			onlyIf { dependsOnTaskDidWork() }
			doLast {
				project.tasks."${PREPARE_TASK_NAME}".repo.with {
					add(patterns: ['.'])
					if (status().clean) {
						println 'Nothing to commit, skipping publish.'
					} else {
						commit(message: extension.commitMessage)
						push()
					}
				}
			}
		}
	}
}
