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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.HashMap;

import org.ajoberstar.gradle.git.api.Commit;
import org.ajoberstar.gradle.git.util.GitUtil;
import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.errors.AmbiguousObjectException;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

/**
 * Task to get the commit log of a local Git repository.
 * The commit log will be stored in a property at the project level.
 * 
 * This can be used to print out a list of commits as follows:
 * <pre><code>
 * task log(type: org.ajoberstar.gradle.git.tasks.GitLog) {
 *   includeRange '580504f', '580504f'
 *   doLast {
 *     log.each { println "$it.abbreviatedId $it.committer.name $it.shortMessage" }
 *   }
 * }
 * </code></pre>
 * 
 * @since 0.2.3
 * @author Alex Lixandru
 */
public class GitLog extends GitBase {
    private List<String> includes;
    private List<String> excludes;
	private int skipCommits = 0;
	private int maxCommits = -1;

    private List<Commit> log;
	
	/**
	 * Gets the commit log and stores it in a project property as configured.
	 */
	@TaskAction
	public void reset() {
		final LogCommand cmd = getGit().log();
        Repository repo = getGit().getRepository();
        if (includes != null) {
            for (String include : includes) {
                try {
                    ObjectId commit = repo.resolve(include);
                    if (commit == null) {
                        throw new GradleException("No commit found for revision string: " + include);
                    } else {
                        cmd.add(commit);
                    }
                } catch (AmbiguousObjectException e) {
                    throw new GradleException("Revision string is ambiguous: " + include, e);
                } catch (MissingObjectException e) {
                    throw new GradleException("Commit could not be found in repository: " + include, e);
                } catch (IncorrectObjectTypeException e) {
                    throw new GradleException("Revision string did not point to a commit: " + include, e);
                } catch (IOException e) {
                    throw new GradleException("Problem resolving revision string: " + include, e);
                }
            }
        }
        if (excludes != null) {
            for (String exclude : excludes) {
                try {
                    ObjectId commit = repo.resolve(exclude);
                    if (commit == null) {
                        throw new GradleException("No commit found for revision string: " + exclude);
                    } else {
                        cmd.add(commit);
                    }                } catch (AmbiguousObjectException e) {
                    throw new GradleException("Revision string is ambiguous: " + exclude, e);
                } catch (MissingObjectException e) {
                    throw new GradleException("Commit could not be found in repository: " + exclude, e);
                } catch (IncorrectObjectTypeException e) {
                    throw new GradleException("Revision string did not point to a commit: " + exclude, e);
                } catch (IOException e) {
                    throw new GradleException("Problem resolving revision string: " + exclude, e);
                }
            }
        }
        cmd.setSkip(skipCommits);
        cmd.setMaxCount(maxCommits);
            
		try {
			Iterable<RevCommit> commits = cmd.call();
			List<Commit> tempLog = new ArrayList<Commit>();
			for (RevCommit commit : commits) {
                tempLog.add(GitUtil.revCommitToCommit(commit));
            }
            this.log = Collections.unmodifiableList(tempLog);
		} catch (GitAPIException e) {
			throw new GradleException("Problem with log.", e);
		}
		//TODO add progress monitor to log progress to Gradle status bar
	}

    /**
     * Gets the list of commits that will
     * be included in the log.
     * @return list of commits
     */
    public List<String> getIncludes() {
        return includes;
    }

    /**
     * Adds to the list of commits to include in the log.
     * @param includes list of commits
     */
    public void include(String... includes) {
        if (this.includes == null) {
            this.includes = new ArrayList<String>(includes.length);
        }
        Collections.addAll(this.includes, includes);
    }

    /**
     * Sets the list of commits to incldue in the log.
     * @param includes list of commits
     */
    public void setIncludes(List<String> includes) {
        this.includes = includes;
    }

    /**
     * Gets the list of commits that will
     * be excluded from the log.
     * @return list of commits
     */
    public List<String> getExcludes() {
        return excludes;
    }

    /**
     * Adds to the list of commits to exclude from the log.
     * @param excludes list of commits
     */
    public void exclude(String... excludes) {
        if (this.excludes == null) {
            this.excludes = new ArrayList<String>(excludes.length);
        }
        Collections.addAll(this.excludes, excludes);
    }

    /**
     * Sets the list of commits to exclude from the log.
     * @param excludes list of commits
     */
    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    /**
     * Adds a range of commits to the include/exclude
     * rules.
     * @param since the commit to begin at
     * @param until the commit to end at
     */
    public void includeRange(String since, String until) {
        include(until);
        exclude(since);
    }

    /**
     * Gets the number of commits from the log to skip before 
     * getting the commit output. Defaults to 0.
     * @return the number of commits to skip
     */
	@Input
    public int getSkipCommits() {
        return skipCommits;
    }

    /**
     * Sets the number of commits from the log to skip before 
     * getting the commit output. Defaults to 0 if not set.
     * @param skipCommits the number of commits to set
     */
    public void setSkipCommits(int skipCommits) {
        this.skipCommits = skipCommits;
    }

    /**
     * Gets the maximum number of commits to get from the commit log. 
     * Defaults to -1 (all commits will be retrieved). 
     * @return the maximum number of commits to retrieve
     */
    @Input
    public int getMaxCommits() {
        return maxCommits;
    }

    /**
     * Sets the maximum number of commits to get from the commit log.
     * Defaults to -1 if not set.
     * @param maxCommits the maximum number of commits to retrieve
     */
    public void setMaxCommits(int maxCommits) {
        this.maxCommits = maxCommits;
    }

    /**
     * Gets the resulting log from this task.
     * @return the log of commits retrieved by
     * this task
     */
    public List<Commit> getLog() {
        if (log == null) {
            throw new IllegalStateException("Task has not executed yet.");
        }
        return log;
    }
}
