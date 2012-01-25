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

import groovy.lang.Closure;

import org.ajoberstar.gradle.util.ObjectUtil;
import org.eclipse.jgit.api.CommitCommand;
import org.eclipse.jgit.lib.PersonIdent;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.ConfigureUtil;

/**
 * 
 * @since 0.1.0
 */
public class GitCommitTask extends GitBaseTask {
	private Object message = null;
	private boolean commitAll = false;
	private PersonIdent committer = null;
	private PersonIdent author = null;
	
	@TaskAction
	public void commit() {
		CommitCommand cmd = getGit().commit();
		cmd.setMessage(getMessage());
		cmd.setAll(getCommitAll());
		if (committer != null) {
			cmd.setCommitter(getCommitter());
		}
		if (author != null) {
			cmd.setAuthor(getAuthor());
		}
		try {
			cmd.call();
		} catch (Exception e) {
			throw new GradleException("Problem committing changes.", e);
		}
	}
	
	@Input
	public String getMessage() {
		return ObjectUtil.unpackString(message);
	}
	
	public void setMessage(Object message) {
		this.message = message;
	}
	
	@Input
	public boolean getCommitAll() {
		return commitAll;
	}
	
	public void setCommitAll(boolean commitAll) {
		this.commitAll = commitAll;
	}
	
	@Input
	@Optional
	public PersonIdent getCommitter() {
		return committer;
	}
	
	public void setCommitter(PersonIdent committer) {
		this.committer = committer;
	}
	
	public void committer(Closure config) {
		if (committer == null) {
			this.committer = new PersonIdent(getGit().getRepository());
		}
		ConfigureUtil.configure(config, committer);
	}
	
	@Input
	@Optional
	public PersonIdent getAuthor() {
		return author;
	}
	
	public void setAuthor(PersonIdent author) {
		this.author = author;
	}
	
	public void author(Closure config) {
		if (author == null) {
			this.author = new PersonIdent(getGit().getRepository());
		}
		ConfigureUtil.configure(config, author);
	}
}
