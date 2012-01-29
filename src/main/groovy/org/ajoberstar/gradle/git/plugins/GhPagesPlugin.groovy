/* Copyright 2012 the original author or authors.
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

import org.ajoberstar.gradle.git.tasks.GitAdd
import org.ajoberstar.gradle.git.tasks.GitBase
import org.ajoberstar.gradle.git.tasks.GitClone
import org.ajoberstar.gradle.git.tasks.GitCommit
import org.ajoberstar.gradle.git.tasks.GitPush
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskCollection

/**
 * Plugin to enable publishing to gh-pages branch of Github.
 * @since 0.1.0
 */
class GhPagesPlugin implements Plugin<Project> {
	static final String GROUP_NAME = 'ghpages'
	static final String CLONE_TASK_NAME = 'cloneGhPages'
	static final String PROCESS_TASK_NAME = 'processGhPages'
	static final String ADD_TASK_NAME = 'addGhPages'
	static final String COMMIT_TASK_NAME = 'commitGhPages'
	static final String PUSH_TASK_NAME = 'pushGhPages'
	static final String PUBLISH_TASK_NAME = 'publishGhPages'
	
	/**
	 * Applies the plugin to the given project.
	 * @param project the project
	 */
	void apply(Project project) {
		project.plugins.apply(GithubPlugin)
		GithubPluginExtension extension = project.extensions.getByType(GithubPluginExtension)
		configureTasks(project, extension)
		
		TaskCollection tasks = project.tasks.matching { it.name.endsWith('GhPages') }
		
		tasks.all {
			it.group = GROUP_NAME
		}
		
		tasks.withType(GitBase) {
			it.repoPath = { extension.ghpages.destinationPath }
		}
	}
	
	/**
	 * Configures the tasks to publish to gh-pages.
	 * @param project the project to configure
	 * @param extension the plugin extension
	 */
	private void configureTasks(final Project project, final GithubPluginExtension extension) {		
		GitClone clone = project.tasks.add(CLONE_TASK_NAME, GitClone)
		clone.description = 'Clones the Github repo checking out the gh-pages branch'
		clone.credentials = extension.credentials
		clone.uri = { extension.repoUri }
		clone.branch = 'gh-pages'
		clone.destinationPath = { extension.ghpages.destinationPath }
		
		Copy process = project.tasks.add(PROCESS_TASK_NAME, Copy)
		process.description = 'Processes the gh-pages files, copying them to the working repo'
		process.dependsOn clone
		process.with extension.ghpages.distribution
		process.into { extension.ghpages.destinationPath }
		
		GitAdd add = project.tasks.add(ADD_TASK_NAME, GitAdd)
		add.description = 'Adds all changes to the working gh-pages repo'
		add.dependsOn process
		
		GitCommit commit = project.tasks.add(COMMIT_TASK_NAME, GitCommit)
		commit.description = 'Commits all changes to the working gh-pages repo'
		commit.dependsOn add
		commit.message = 'Publish of github pages from Gradle'
		
		GitPush push = project.tasks.add(PUSH_TASK_NAME, GitPush)
		push.description = 'Pushes all changes in the working gh-pages repo to Github'
		push.dependsOn commit
		push.credentials = extension.credentials
		
		Task publish = project.tasks.add(PUBLISH_TASK_NAME)
		publish.description = 'Publishes all gh-pages changes to Github'
		publish.dependsOn push
	}
}
