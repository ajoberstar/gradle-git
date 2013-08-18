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

import org.eclipse.jgit.api.InitCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;

import java.io.File;

/**
 * Task to initialize a new Git repository on the local system.
 * @since 0.6.2
 * @author Rasmus 'Cervator' Praestholm <cervator@gmail.com>
 */
public class GitInit extends DefaultTask
{
	private boolean bare = false;
	private Object destinationPath = null;

	/**
	 * Initializes a new local Git repository as configured.
	 */
	@TaskAction
	public void initRepo() {
        InitCommand cmd = new InitCommand();
		cmd.setBare(getBare());
		cmd.setDirectory(getDestinationDir());
		try {
			cmd.call();
		} catch (GitAPIException e) {
			throw new GradleException("Problem with initializing new Git repo.", e);
		}
	}
	
	/**
	 * Gets whether the repository will be bare.
	 * @return whether the repo will be bare
	 */
	@Input
	public boolean getBare() {
		return bare;
	}
	
	/**
	 * Sets whether the repository will be bare.
	 * @param bare whether the repo will be bare
	 */
	public void setBare(boolean bare) {
		this.bare = bare;
	}
	
	/**
	 * Gets the destination directory the repository
	 * will be initialized into.
	 * @return the path to initialized into
	 */
	@OutputDirectory
	public File getDestinationDir() {
		return getProject().file(destinationPath);
	}
	
	/**
	 * Sets the path the repository should be initialized into.
	 * Will be evaluated using {@link org.gradle.api.Project#file(Object)}.
	 * @param destinationPath the path to initialize into
	 */
	public void setDestinationPath(Object destinationPath) {
		this.destinationPath = destinationPath;
	}
}
