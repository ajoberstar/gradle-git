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

import java.util.ArrayList;
import java.util.HashMap;

import org.eclipse.jgit.api.LogCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

/**
 * Task to get the commit log of a local Git repository.
 * The commit log will be stored in a property at the project level.
 * 
 * @since 0.2.3
 * @author Alex Lixandru
 */
public class GitLog extends GitBase {
	private int skip = 0;
	private int count = 1;
	private String property = "changelog";
	
	/**
	 * Gets the commit log and stores it in a project property as configured.
	 */
	@TaskAction
	public void reset() {
		final LogCommand cmd = getGit().log();
		
		cmd.setMaxCount(count);
		cmd.setSkip(skip);
		
		try {
			Iterable<RevCommit> commits = cmd.call();
			ArrayList<HashMap<String, String>> log = new ArrayList<HashMap<String,String>>();
			for (RevCommit commit : commits) {
			    HashMap<String, String> c = new HashMap<String, String>();
			    c.put("commit", commit.getName());
			    c.put("date", String.valueOf(commit.getCommitTime()));
			    c.put("message", commit.getFullMessage());
			    c.put("shortMessage", commit.getShortMessage());
			    c.put("author", commit.getAuthorIdent().getName());
			    c.put("authorEmail", commit.getAuthorIdent().getEmailAddress());
			    c.put("commiter", commit.getCommitterIdent().getName());
			    c.put("commiterEmail", commit.getCommitterIdent().getEmailAddress());
			    c.put("parent", commit.getParent(0).getName());
			    
			    log.add(c);
            }
			
			getProject().getExtensions().add(property, log);
			
		} catch (GitAPIException e) {
			throw new GradleException("Problem with log.", e);
		}
		//TODO add progress monitor to log progress to Gradle status bar
	}

    /**
     * Gets the number of commits from the log to skip before 
     * getting the commit output. Defaults to 0.
     * @return the number of commits to skip
     */
	@Input
    public int getSkip() {
        return skip;
    }

    /**
     * Sets the number of commits from the log to skip before 
     * getting the commit output. Defaults to 0 if not set.
     * @param skip the number of commits to set
     */
    public void setSkip(int skip) {
        this.skip = skip;
    }

    /**
     * Gets the maximum number of commits to get from the commit log. 
     * Defaults to 1 (only the last commit log will be retrieved). 
     * @return the count
     */
    @Input
    public int getCount() {
        return count;
    }

    /**
     * Sets the maximum number of commits to get from the commit log.
     * Defaults to 1 if not set.
     * @param count the number of commits to get
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * Gets the name of the property where commit log should be put. 
     * Defaults to {@code changelog}.
     * @return the name of the property
     */
    @Input
    public String getProperty() {
        return property;
    }

    /**
     * Sets the name of the property which will contain the commit log.
     * <p>If not set the default value {@code changelog} will be used. 
     * After this task executes the commit log will be accessible for
     * further processing through {@code project.changelog} property 
     * or whatever property name is set by this method.
     * </p>
     * @param property the property where to put the commit log
     */
    public void setProperty(String property) {
        this.property = property;
    }

}
