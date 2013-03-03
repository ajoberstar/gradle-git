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

import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.tasks.TaskAction;

import java.util.Set;

/**
 * Gets the status of all files in the repository.
 * @since 0.3.0
 * @author Evgeny Shepelyuk
 */
public class GitStatus extends GitBase {
	private Status status;

	@TaskAction
	public void status() {
		StatusCommand cmd = getGit().status();
		try {
			this.status = cmd.call();
		} catch (GitAPIException e) {
			throw new GradleException("Problem obtaining status", e);
		}
	}

	/**
	 * Gets all new files added to the index, but not
	 * yet commited to HEAD.
	 * @return file collection of added files
	 */
	public FileCollection getAdded() {
		assertComplete();
		return toFiles(status.getAdded());
	}

	/**
	 * Gets all existing files with changes added to
	 * the index, that have not been committed to HEAD.
	 * @return file collection of changed files
	 */
	public FileCollection getChanged() {
		assertComplete();
		return toFiles(status.getChanged());
	}

	/**
	 * Gets all files that are in conflict. This will include
	 * files modified by you, but also modified by someone
	 * else beforehand.
	 * @return file collection of conflicting files
	 */
	public FileCollection getConflicting() {
		assertComplete();
		return toFiles(status.getConflicting());
	}

	/**
	 * Get files that have been ignored and
	 * aren't in the index.
	 * @return file collection of ignored files
	 */
	public FileCollection getIgnored() {
		assertComplete();
		return toFiles(status.getIgnoredNotInIndex());
	}
	/**
	 * Get files that have been deleted from the filesystem,
	 * but have not been removed from the index yet.
	 * @return file collection of missing files
	 */
	public FileCollection getMissing() {
		assertComplete();
		return toFiles(status.getMissing());
	}

	/**
	 * Gets all existing files that have
	 * been modified, but the changes
	 * aren't in the index.
	 * @return file collection of modified files
	 */
	public FileCollection getModified() {
		assertComplete();
		return toFiles(status.getModified());
	}

	/**
	 * Gets all files that have been removed
	 * from the index.
	 * @return file collection of removed files
	 */
	public FileCollection getRemoved() {
		assertComplete();
		return toFiles(status.getRemoved());
	}

	/**
	 * Gets all files that have not been ignored
	 * or added to the index.
	 * @return file collection of untracked files
	 */
	public FileCollection getUntracked() {
		assertComplete();
		return toFiles(status.getUntracked());
	}

	/**
	 * Gets all directories that have not been
	 * ignored or added to the index.
	 * @return file collection of untracked directories.
	 */
	public FileCollection getUntrackedDirs() {
		assertComplete();
		return toFiles(status.getUntrackedFolders());
	}

	/**
	 * Converts the set of given paths to a file collection.
	 * @param paths set of file paths
	 * @return file collection representing the paths
	 */
	private FileCollection toFiles(Set<String> paths) {
		return ((ProjectInternal) getProject()).getFileResolver().withBaseDir(getRepoDir()).resolveFiles(paths);
	}

	/**
	 * Verifies that the task has executed.
	 * @throws IllegalStateException if task not started
	 */
	private void assertComplete() {
		if (status == null) {
			throw new IllegalStateException("Task has not executed yet.");
		}
	}
}
