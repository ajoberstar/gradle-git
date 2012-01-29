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

import org.ajoberstar.gradle.util.ObjectUtil
import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.AuthenticationSupported
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.file.CopySpec;
import org.gradle.util.ConfigureUtil

/**
 * Extension for Github properties.
 * @since 0.1.0
 */
class GithubPluginExtension implements AuthenticationSupported {
	private final PasswordCredentials credentials = new BasicPasswordCredentials()
	/**
	 * Ghpages properties.
	 */
	final GhPagesPluginExtension ghpages
	
	/**
	 * The URI of the Github repository.
	 */
	Object repoUri
	
	/**
	 * Constructs the plugin extension.
	 * @param project the project to create
	 * the extension for
	 */
	GithubPluginExtension(Project project) {
		this.ghpages = new GhPagesPluginExtension(project)
	}
	
	/**
	 * Gets the URI of the Github repository. 
	 * @return the repo URI
	 */
	public String getRepoUri() {
		return ObjectUtil.unpackString(repoUri)
	}
	
	/**
	 * {@inheritDoc}
	 */
	public PasswordCredentials getCredentials() {
		return credentials
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void credentials(Closure closure) {
		ConfigureUtil.configure(closure, getCredentials())
	}
}
