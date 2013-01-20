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

import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRefNameException;
import org.eclipse.jgit.api.errors.RefAlreadyExistsException;
import org.eclipse.jgit.api.errors.RefNotFoundException;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

/**
 * Creates a new branch in a Git repository.
 * @since 0.3.0
 */
public class GitBranch extends GitBase {
    /**
     * Tracking mode for branches.
     */
    public static enum Mode {
        NO_TRACK,
        TRACK,
        SET_UPSTREAM;
    }

    private String branchName;
    private String startPoint = "master";
    private Mode mode;
    private boolean force = false;

    /**
     * Execute the creation or update of the branch.
     */
    @TaskAction
    void branchCreate() {
        CreateBranchCommand cmd = getGit().branchCreate();
        cmd.setName(getBranchName());
        cmd.setStartPoint(getStartPoint());
        cmd.setForce(getForce());

        if (getMode() != null) {
            switch(getMode()) {
                case NO_TRACK:
                    cmd.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.NOTRACK);
                    break;
                case TRACK:
                    cmd.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.TRACK);
                    break;
                case SET_UPSTREAM:
                    cmd.setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM);
                    break;
                default:
                    throw new AssertionError("Illegal mode: " + getMode());
            }
        }

        try {
            cmd.call();
        } catch (InvalidRefNameException e) {
            throw new GradleException("Invalid branch name: " + getName(), e);
        } catch (RefNotFoundException e) {
            throw new GradleException("Can't find start point: " + getStartPoint(), e);
        } catch (RefAlreadyExistsException e) {
            throw new GradleException("Branch " + getName() + " already exists. Use force=true to modify.", e);
        } catch (GitAPIException e) {
            throw new GradleException("Problem creating or updating branch " + getName(), e);
        }
    }

    /**
     * Gets the name of the branch to create or update.
     * @return branchName of the branch
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * Sets the name of the branch to create or update.
     * @param branchName the name of the branch
     */
    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    /**
     * Gets the starting point of the branch.
     * @return the starting point of the branch
     */
    public String getStartPoint() {
        return startPoint;
    }

    /**
     * Sets the starting point of the branch.
     * @param startPoint the start point of the branch
     */
    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    /**
     * Gets the tracking mode of the branch.
     * @return the tracking mode
     */
    public Mode getMode() {
        return mode;
    }

    /**
     * Sets the tracking mode of the branch.
     * @param mode the tracking mode
     */
    public void setMode(Mode mode) {
        this.mode = mode;
    }

    /**
     * Gets whether an existing branch will be modified
     * by this task.
     * @return whether to force changes to existing
     * branches
     */
    public boolean getForce() {
        return force;
    }

    /**
     * Sets whether an existing branch will be modified
     * by this task.
     * @param force {@code true} if existing branches
     * will be updated, {@code false} if the task should
     * fail if the branch exists
     */
    public void setForce(boolean force) {
        this.force = force;
    }
}
