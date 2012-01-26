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

/**
 * 
 * @since 0.1.0
 */
class GhPagesPlugin implements Plugin<Project> {
	static final String CLONE_TASK_NAME = 'ghPagesClone'
	static final String PROCESS_TASK_NAME = 'processGhPages'
	static final String ADD_TASK_NAME = 'ghPagesAdd'
	static final String COMMIT_TASK_NAME = 'ghPagesCommit'
	static final String PUSH_TASK_NAME = 'ghPagesPush'
	static final String PUBLISH_TASK_NAME = 'publishGhPages'
	
	void apply(Project project) {
		project.plugins.apply(GitPlugin)
		GhPagesPluginExtension extension = new GhPagesPluginExtension(project)
		project.extensions.ghpages = extension
		configureTasks(project, extension)
		
		project.tasks.matching { it.name ==~ /^ghPages.*/ }.withType(GitBase) {
			it.repoPath = { extension.destinationPath }
		}
	}

	private void configureTasks(final Project project, final GhPagesPluginExtension extension) {
		GitClone clone = project.tasks.add(CLONE_TASK_NAME, GitClone)
		clone.uri = { extension.githubRepoUri }
		clone.branch = 'gh-pages'
		clone.destinationPath = { extension.destinationPath }
		
		Copy process = project.tasks.add(PROCESS_TASK_NAME, Copy)
		process.dependsOn clone
		process.with extension.getGhpagesDistribution()
		process.into { extension.destinationPath }
		
		GitAdd add = project.tasks.add(ADD_TASK_NAME, GitAdd)
		add.dependsOn process
		
		GitCommit commit = project.tasks.add(COMMIT_TASK_NAME, GitCommit)
		commit.dependsOn add
		commit.message = 'Publish of github pages from Gradle'
		
		GitPush push = project.tasks.add(PUSH_TASK_NAME, GitPush)
		push.dependsOn commit
		push.credentials = extension.credentials
		
		Task publish = project.tasks.add(PUBLISH_TASK_NAME)
		publish.dependsOn push
	}
}
