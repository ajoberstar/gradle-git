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
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.api.errors.ConcurrentRefUpdateException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidTagNameException;
import org.eclipse.jgit.api.errors.NoHeadException;
import org.eclipse.jgit.lib.PersonIdent;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.ConfigureUtil;

/**
 * Task to tag a commit in a Git repository.
 * @since 0.2.0
 */
public class GitTag extends GitBase {
	private Object tagName = null;
	private Object message = null;
	private boolean sign = false;
	private boolean force = false;
	private PersonIdent tagger = null;

	/**
	 * Tags the HEAD.
	 */
	@TaskAction
	public void tag() {
		TagCommand cmd = getGit().tag();
		cmd.setName(getTagName());
		cmd.setMessage(getMessage());
		cmd.setSigned(getSign());
		cmd.setForceUpdate(getForce());
		if (tagger != null) {
			cmd.setTagger(getTagger());
		}
		
		try {
			cmd.call();
		} catch (ConcurrentRefUpdateException e) {
			throw new GradleException("Another process is accessing or updating the ref.", e);
		} catch (InvalidTagNameException e) {
			throw new GradleException("Invalid tag name: " + getTagName(), e);
		} catch (NoHeadException e) {
			throw new GradleException("Cannot tag without a HEAD revision.", e);
		} catch (GitAPIException e) {
			throw new GradleException("Problem with tag.", e);
		}
	}

	/**
	 * Gets the tag message to use.
	 * @return the tag message to use
	 */
	@Input
	@Optional
	public String getMessage() {
		return ObjectUtil.unpackString(message);
	}

	/**
	 * Sets the tag message to use.
	 * @param message the tag message
	 */
	public void setMessage(Object message) {
		this.message = message;
	}

	/**
	 * Gets the tag name to use.
	 * @return the tag name to use
	 */
	@Input
	public String getTagName() {
		return ObjectUtil.unpackString(tagName);
	}

	/**
	 * Sets the tag name to use.
	 * @param tagName the tag name
	 */
	public void setTagName(Object tagName) {
		this.tagName = tagName;
	}
	
	/**
	 * Gets whether or not the tag will be signed with
	 * the tagger's default key.
	 * 
	 * This defaults to false.
	 * @return {@code true} if the tag will be signed,
	 * {@code false} otherwise
	 */
	@Input
	public boolean getSign() {
		return sign;
	}
	
	/**
	 * Sets whether or not the tag will be signed
	 * by the tagger's default key.
	 * @param sign {@code true} if the tag should 
	 * be signed, {@code false} otherwise
	 */
	public void setSign(boolean sign) {
		this.sign = sign;
	}
	
	/**
	 * Gets whether or not the tag will be created/updated even
	 * if a tag of that name already exists.
	 * 
	 * This defaults to false.
	 * @return {@code true} if the tag will be forced,
	 * {@code false} otherwise
	 */
	@Input
	public boolean getForce() {
		return force;
	}
	
	/**
	 * Sets whether or not the tag will be created/updated
	 * even if a tag of that name already exists.
	 * @param force {@code true} if the tag will be forced,
	 * {@code false} otherwise
	 */
	public void setForce(boolean force) {
		this.force = force;
	}
	
	/**
	 * Gets the tagger.
	 * @return the tagger
	 */
	@Input
	@Optional
	public PersonIdent getTagger() {
		return tagger;
	}

	/**
	 * Sets the tagger.
	 * @param tagger the tagger
	 */
	public void setTagger(PersonIdent tagger) {
		this.tagger = tagger;
	}

	/**
	 * Configures the tagger.
	 * A {@code PersonIdent} is passed to the closure.
	 * @param config the configuration closure
	 */
	@SuppressWarnings("rawtypes")
	public void tagger(Closure config) {
		if (tagger == null) {
			this.tagger = new PersonIdent(getGit().getRepository());
		}
		ConfigureUtil.configure(config, tagger);
	}
}
