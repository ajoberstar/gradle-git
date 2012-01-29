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

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin for supporting Github.
 * @since 0.1.0
 */
class GithubPlugin implements Plugin<Project> {
	static final String USERNAME_PROP = 'github.credentials.username'
	static final String PASSWORD_PROP = 'github.credentials.password'
	
	/**
	 * Applies the plugin to the given project.
	 * @param project the project
	 */
	void apply(Project project) {
		project.plugins.apply(GitPlugin)
		GithubPluginExtension extension = new GithubPluginExtension(project)
		project.extensions.add('github', extension)	
		setDefaultCredentials(project, extension)
	}
	
	/**
	 * Sets the default credentials based on project properties.
	 * @param project the project to get properties from
	 * @param extension the extension to configure credentials for
	 */
	private void setDefaultCredentials(Project project, GithubPluginExtension extension) {
		if (project.hasProperty(USERNAME_PROP)) {
			extension.credentials.username = project[USERNAME_PROP]
		}
		if (project.hasProperty(PASSWORD_PROP)) {
			extension.credentials.password = project[PASSWORD_PROP]
		}
	}
}
