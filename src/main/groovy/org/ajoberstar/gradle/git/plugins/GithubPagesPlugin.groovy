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
package org.ajoberstar.gradle.git.plugins

import org.ajoberstar.grgit.Grgit

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

/**
 * Plugin to enable publishing to gh-pages branch of Github.
 * @since 0.1.0
 */
class GithubPagesPlugin implements Plugin<Project> {
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
		Task publish = project.tasks.create(PUBLISH_TASK_NAME)
		publish.description = 'Publishes all gh-pages changes to Github'
		publish.doLast {
			extension.workingDir.deleteDir()
			repo = Grgit.clone {
				uri = extension.repoUri
				refToCheckout = extension.targetBranch
				dir = extension.workingDir
				credentials = extension.credentials?.toGrgit()
			}
			project.copy {
				with extension.pages
				into repo.repository.rootDir
			}
			repo.add(patterns: ['.'], update: true)
			repo.add(patterns: ['.'])
			repo.commit(message: 'Publish of Github pages from Gradle.')
			repo.push()
		}
	}
}
