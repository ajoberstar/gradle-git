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

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.gradle.api.DefaultTask;
import org.gradle.api.UncheckedIOException;

/**
 * Base task for all Git commands that
 * are executed on an existing repository.
 * @since 0.1.0
 */
public abstract class GitBase extends DefaultTask {
	private Object repoPath = getProject().getRootProject().getProjectDir();
	
	/**
	 * Gets the directory of the repository.
	 * @return the repo directory
	 */
	protected File getRepoDir() {
		return getProject().file(repoPath);
	}
	
	/**
	 * Sets the path to the repository.  Will be
	 * evaluated relative to the project directory
	 * @param repoPath the path to the repository
	 */
	public void setRepoPath(Object repoPath) {
		this.repoPath = repoPath;
	}
	
	/**
	 * Gets a Git instance for this task's repository.
	 * @return a new Git instance
	 */
	protected Git getGit() {
		try {
			return Git.open(getRepoDir());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
