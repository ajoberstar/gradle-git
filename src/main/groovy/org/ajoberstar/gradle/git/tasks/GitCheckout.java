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

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.lib.Constants;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.tasks.TaskAction;

/**
 * Task to checkout files and refs
 * @author Evgeny Shepelyuk
 * @since 0.3.0
 */
public class GitCheckout extends GitSource {
    private String startPoint;

    private String branchName = Constants.MASTER;

    private boolean createBranch = false;

    @TaskAction
    void checkout() {
        final CheckoutCommand cmd = getGit().checkout();
        cmd.setStartPoint(startPoint);
        cmd.setName(branchName);
        cmd.setCreateBranch(createBranch);

        if (!patternSet.getExcludes().isEmpty() || !patternSet.getIncludes().isEmpty()) {
            getSource().visit(new FileVisitor() {
                public void visitDir(FileVisitDetails arg0) {
                    visitFile(arg0);
                }

                public void visitFile(FileVisitDetails arg0) {
                    cmd.addPath(arg0.getPath());
                }
            });
        }

        try {
            cmd.call();
        } catch (Exception e) {
            throw new GradleException("Problem checking out from repository", e);
        }
    }

    /**
     * Gets the start point for the new branch being created.
     * @return the name of the commit to start at
     */
    public String getStartPoint() {
        return startPoint;
    }

    /**
     * Set starting point for the new branch being created.
     * @param startPoint the name of the commit to start at
     */
    public void setStartPoint(String startPoint) {
        this.startPoint = startPoint;
    }

    /**
     * Gets the branch or commit name to check out.
     * @return name of branch or commit
     */
    public String getBranchName() {
        return branchName;
    }

    /**
     * Specify the name of the branch or commit to check out, or the new branch name.
     * @param branchName
     */
    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    /**
     * Gets whether the branch being checked out should be created.
     * @return whether to create the branch
     */
    public boolean getCreateBranch() {
        return createBranch;
    }

    /**
     * Specify whether to create a new branch.
     * @param createBranch
     */
    public void setCreateBranch(boolean createBranch) {
        this.createBranch = createBranch;
    }
}
