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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.ajoberstar.gradle.util.ObjectUtil;
import org.eclipse.jgit.api.CleanCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

/**
 * Task to clean the working tree of a local Git repository.
 * 
 * @since 0.2.3
 * @author Alex Lixandru
 */
public class GitClean extends GitBase {
	private Set<Object> paths = null;
	
	/**
	 * Reset the changes as configured.
	 * If {@code paths} is set, only the specified
	 * paths will be cleaned.  Otherwise all paths
	 * will be.
	 */
	@TaskAction
	public void reset() {
		final CleanCommand cmd = getGit().clean();
		
		cmd.setPaths(getPaths());
		
		try {
			cmd.call();
		} catch (GitAPIException e) {
			throw new GradleException("Problem with clean.", e);
		}		
		//TODO add progress monitor to log progress to Gradle status bar
	}

	/**
	 * Gets the paths to clean.
	 * @return the paths to clean
	 */
	@Input
	public Set<String> getPaths() {
		if (paths == null) {
			return Collections.emptySet();
		}
		Set<String> cleanPaths = new HashSet<String>();
		for (Object path : paths) {
			cleanPaths.add(ObjectUtil.unpackString(path));
		}
		return cleanPaths;
	}
	
	/**
	 * Adds paths to be cleaned. 
	 * @param cleanPaths the paths to be cleaned
	 */
	public void paths(Object... cleanPaths) {
		if (paths == null) {
			this.paths = new HashSet<Object>();
		}
		Collections.addAll(paths, cleanPaths);
	}

	/**
	 * Adds paths to be cleaned.
	 * @param cleanPaths the paths to be cleaned
	 */
	public void paths(Iterable<? extends Object> cleanPaths) {
		if (paths == null) {
			this.paths = new HashSet<Object>();
		}
		for (Object path : cleanPaths) {
			this.paths.add(path);
		}
	}
	
	/**
	 * Sets the paths for the clean command.
	 * @param cleanPaths the paths to be cleaned
	 */
	public void setPaths(Set<Object> cleanPaths) {
		this.paths = cleanPaths;
	}
}
