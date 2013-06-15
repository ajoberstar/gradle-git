/*
 * Copyright 2012-2013 the original author or authors.
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
package org.ajoberstar.gradle.git.tasks;

import groovy.lang.Closure;

import org.ajoberstar.gradle.git.auth.BasicPasswordCredentials;
import org.ajoberstar.gradle.git.auth.TransportAuthUtil;
import org.eclipse.jgit.api.PullCommand;
import org.eclipse.jgit.api.errors.*;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.repositories.AuthenticationSupported;
import org.gradle.api.artifacts.repositories.PasswordCredentials;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.ConfigureUtil;

/**
 * Task to incorporate changes from a remote repository into the current 
 * branch of a local Git repository.
 *  
 * @since 0.2.1
 * @author Alex Lixandru
 */
public class GitPull extends GitBase implements AuthenticationSupported {
	private PasswordCredentials credentials = new BasicPasswordCredentials();
	
	/**
	 * Pulls changes from a remote Git repository and merges them into the 
	 * current branch.
	 */
	@TaskAction
	public void pullRepo() {
		PullCommand cmd = getGit().pull();
		TransportAuthUtil.configure(cmd, this);
		try {
			cmd.call();
		} catch (InvalidRemoteException e) {
			throw new GradleException("Invalid remote repository.", e);
		} catch (TransportException e) {
			throw new GradleException("Problem with transport.", e);
		} catch (WrongRepositoryStateException e) {
			throw new GradleException("Invalid repository state.", e);
		} catch (GitAPIException e) {
			throw new GradleException("Problem with pull.", e);
		}
		//TODO add progress monitor to log progress to Gradle status bar
	}
	
	/**
	 * Gets the credentials to be used when pulling from the repo.
	 * @return the credentials
	 */
	@Input
	@Optional
	public PasswordCredentials getCredentials() {
		return credentials;
	}
	
	/**
	 * Configured the credentials to be used when pulling from the repo.
	 * This will be passed a {@link PasswordCredentials} instance.
	 * @param closure the configuration closure
	 */
	@SuppressWarnings("rawtypes")
	public void credentials(Closure closure) {
		ConfigureUtil.configure(closure, getCredentials());
	}
	
	/**
	 * Sets the credentials to use when pulling from the repo.
	 * @param credentials the credentials
	 */
	public void setCredentials(PasswordCredentials credentials) {
		this.credentials = credentials;
	}
}
