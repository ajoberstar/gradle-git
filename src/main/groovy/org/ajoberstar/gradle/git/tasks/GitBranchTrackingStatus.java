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

import org.ajoberstar.gradle.git.api.TrackingStatus;
import org.ajoberstar.gradle.git.util.GitUtil;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.Constants;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.io.IOException;

/**
 * Gets the remote tracking status of branche.
 *
 * @author Evgeny Shepelyuk
 * @since 0.3.0
 */
public class GitBranchTrackingStatus extends GitBase {

    private TrackingStatus trackingStatus;

    private String localBranch = Constants.MASTER;

//    private static final TrackingStatus NO_TRACKING_BRANCH = new TrackingStatus(null, -1, -1);

    /**
     * Set name of local branch to get remote tracking status
     *
     * @param localBranch
     */
    public void setLocalBranch(String localBranch) {
        this.localBranch = localBranch;
    }

    @TaskAction
    public void trackingStatuses() {
        try {
            trackingStatus = GitUtil.trackingStatusFromGit(BranchTrackingStatus.of(getGit().getRepository(), localBranch));
        } catch (IOException e) {
            throw new GradleException("Problem listing branches", e);
        }
    }

    /**
     * Return remote tracking branch status
     *
     * @return tracking status object
     */
    public TrackingStatus getTrackingStatus() {
        if (null == trackingStatus) {
            throw new IllegalStateException("Task has not executed yet.");
        }
        return trackingStatus;
    }
}
