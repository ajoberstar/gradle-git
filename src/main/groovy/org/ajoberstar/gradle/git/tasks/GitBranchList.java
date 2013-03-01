/* Copyright 2013 the original author or authors.
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

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;
import org.ajoberstar.gradle.git.api.Branch;
import org.ajoberstar.gradle.git.util.GitUtil;

/**
 * Lists branches in a Git repository.
 * @since 0.3.0
 * @author Evgeny Shepelyuk
 */
public class GitBranchList extends GitBase {
	public static enum BranchType {
		LOCAL,
		REMOTE,
		ALL
	}

	private BranchType type = BranchType.LOCAL;
	private List<Branch> branches;
	private Branch workingBranch;

	/**
	 * Execute command to list branches.
	 */
	@TaskAction
	void branchList() {
		ListBranchCommand cmd = getGit().branchList();
		if (getBranchType() != null) {
			switch (getBranchType()) {
				case REMOTE:
					cmd.setListMode(ListBranchCommand.ListMode.REMOTE);
					break;
				case ALL:
					cmd.setListMode(ListBranchCommand.ListMode.ALL);
					break;
				case LOCAL:
					break; //use default
				default:
					throw new AssertionError("Illegal branch type: " + getBranchType());
			}
		}
		try {
			Repository repo = getGit().getRepository();
			branches = new ArrayList<Branch>();
			for (Ref ref : cmd.call()) {
				branches.add(GitUtil.refToBranch(repo, ref));
			}
			workingBranch = GitUtil.gitNameToBranch(repo, repo.getFullBranch());
		} catch (IOException e) {
			throw new GradleException("Problem listing branches", e);
		} catch (GitAPIException e) {
			throw new GradleException("Problem listing branches", e);
		}
	}

	/**
	 * Gets the type of branches to retrieve.
	 * @return the type
	 */
	public BranchType getBranchType() {
		return type;
	}

	/**
	 * Sets the type of branches to retrieve.
	 * @param type 
	 */
	public void setBranchType(BranchType type) {
		this.type = type;
	}

	/**
	 * Gets the branches retrieved by the task.
	 * @return list of branches
	 * @throws IllegalStateException if the task has not executed yet
	 */
	public List<Branch> getBranches() {
		if (branches == null) {
			throw new IllegalStateException("Task has not executed yet.");
		}
		return branches;
	}

	/**
	 * Gets the current working branch of this repository.
	 * @return the working branch
	 */
	public Branch getWorkingBranch() {
		if (workingBranch == null) {
			throw new IllegalStateException("Task has not executed yet.");
		}
		return workingBranch;
	}
}
