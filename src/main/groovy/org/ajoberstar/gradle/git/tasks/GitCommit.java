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
import org.ajoberstar.gradle.util.ObjectUtil;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.lib.PersonIdent;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.ConfigureUtil;

/**
 * Task to commit changes to a Git repository.
 *
 * @since 0.1.0
 */
public class GitCommit extends GitSource {

    private Object message = null;
    private boolean commitAll = false;
    private PersonIdent committer = null;
    private PersonIdent author = null;

    /**
     * Commits changes to the Git repository.
     */
    @TaskAction
    public void commit() {
        final CommitCommand cmd = getGit().commit();
        cmd.setMessage(getMessage());
        cmd.setAll(getCommitAll());
        if (committer != null) {
            cmd.setCommitter(getCommitter());
        }
        if (author != null) {
            cmd.setAuthor(getAuthor());
        }

        if (!patternSet.getExcludes().isEmpty() || !patternSet.getIncludes().isEmpty()) {
            getSource().visit(new FileVisitor() {
                public void visitDir(FileVisitDetails arg0) {
                    visitFile(arg0);
                }

                public void visitFile(FileVisitDetails arg0) {
                    cmd.setOnly(arg0.getPath());
                }
            });
        }

        try {
            cmd.call();
        } catch (Exception e) {
            throw new GradleException("Problem committing changes.", e);
        }
    }

    /**
     * Gets the commit message to use.
     *
     * @return the commit message to use
     */
    @Input
    public String getMessage() {
        return ObjectUtil.unpackString(message);
    }

    /**
     * Sets the commit message to use.
     *
     * @param message the commit message
     */
    public void setMessage(Object message) {
        this.message = message;
    }

    /**
     * Gets whether to commit all modified and deleted files.
     * New files will not be affected.
     *
     * @return whether to commit all files
     */
    @Input
    public boolean getCommitAll() {
        return commitAll;
    }

    /**
     * Sets whether to commit all modified and deleted files.
     * New files will not be affected.
     *
     * @param commitAll whetherh to commit all files
     */
    public void setCommitAll(boolean commitAll) {
        this.commitAll = commitAll;
    }

    /**
     * Gets the committer to for this commit.
     *
     * @return the committer
     */
    @Input
    @Optional
    public PersonIdent getCommitter() {
        return committer;
    }

    /**
     * Sets the committer for this commit.
     *
     * @param committer the committer
     */
    public void setCommitter(PersonIdent committer) {
        this.committer = committer;
    }

    /**
     * Configures the committer for this commit.
     * A {@code PersonIdent} is passed to the closure.
     *
     * @param config the configuration closure
     */
    @SuppressWarnings("rawtypes")
    public void committer(Closure config) {
        if (committer == null) {
            this.committer = new PersonIdent(getGit().getRepository());
        }
        ConfigureUtil.configure(config, committer);
    }

    /**
     * Gets the author for this commit.
     *
     * @return the author
     */
    @Input
    @Optional
    public PersonIdent getAuthor() {
        return author;
    }

    /**
     * Sets the author for this commit.
     *
     * @param author the author
     */
    public void setAuthor(PersonIdent author) {
        this.author = author;
    }

    /**
     * Configures the author for this commit.
     * A {@code PersonIdent} is passed to the closure.
     *
     * @param config the configuration closure
     */
    @SuppressWarnings("rawtypes")
    public void author(Closure config) {
        if (author == null) {
            this.author = new PersonIdent(getGit().getRepository());
        }
        ConfigureUtil.configure(config, author);
    }
}
