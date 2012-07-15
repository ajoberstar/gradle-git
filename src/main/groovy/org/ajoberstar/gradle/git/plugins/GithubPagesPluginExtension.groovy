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

import org.ajoberstar.gradle.git.auth.BasicAuthenticationSupport
import org.ajoberstar.gradle.git.auth.ConfigurableAuthenticationSupported
import org.ajoberstar.gradle.util.ObjectUtil
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.util.ConfigureUtil

/**
 * Extension for gh-pages specific properties.
 * @since 0.1.0
 */
class GithubPagesPluginExtension{
	private final Project project
	@Delegate private final ConfigurableAuthenticationSupported authSupport = new BasicAuthenticationSupport()
	
	/**
	 * The URI of the Github repository.
	 */
	Object repoUri
	
	/**
	 * The distribution of files to put in gh-pages.
	 */
	final CopySpec pages
	
	/**
	 * The path to put the github repository in.
	 */
	Object workingPath
	
	/**
	 * Constructs the plugin extension.
	 * @param project the project to create
	 * the extension for
	 */
	GithubPagesPluginExtension(Project project) {
		this.project = project;
		this.pages = project.copySpec {
			from 'src/main/ghpages'
		}
	}
	
	/**
	 * Gets the URI of the Github repository.  This
	 * will be used to clone the repository.
	 * @return the repo URI
	 */
	public String getRepoUri() {
		return ObjectUtil.unpackString(repoUri)
	}
	
	/**
	 * Gets the working directory that the repo will be places in.
	 * @return the working directory
	 */
	public File getWorkingDir() {
		return project.file(getWorkingPath() ?: "${project.buildDir}/ghpages")
	}
	
	/**
	 * Configures the gh-pages copy spec.
	 * @param closure the configuration closure
	 */
	void pages(Closure closure) {
		ConfigureUtil.configure(closure, pages)
	}
}
