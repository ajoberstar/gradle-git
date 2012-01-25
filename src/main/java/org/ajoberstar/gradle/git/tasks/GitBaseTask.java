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
package org.ajoberstar.gradle.git.tasks;

import java.io.File;
import java.io.IOException;

import org.eclipse.jgit.api.Git;
import org.gradle.api.DefaultTask;
import org.gradle.api.UncheckedIOException;

/**
 * 
 * 
 * @since 0.1.0
 */
public abstract class GitBaseTask extends DefaultTask {
	private Git git = null;
	private Object repoPath = null;
	
	protected File getRepoDir() {
		return getProject().file(repoPath);
	}
	
	public void setRepoPath(Object repoPath) {
		this.repoPath = repoPath;
	}
	
	protected Git getGit() {
		try {
			return git == null ? Git.open(getRepoDir()) : git;
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
	
	public void setGit(Git git) {
		this.git = git;
	}
}
