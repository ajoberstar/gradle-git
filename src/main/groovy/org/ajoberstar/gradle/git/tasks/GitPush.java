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
import org.eclipse.jgit.api.PushCommand;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.repositories.AuthenticationSupported;
import org.gradle.api.artifacts.repositories.PasswordCredentials;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.ConfigureUtil;

/**
 * 
 * @since 0.1.0
 */
public class GitPush extends GitBase implements AuthenticationSupported {
	private PasswordCredentials credentials = null;
	private Object remote = null;
	private boolean pushTags = false;
	private boolean pushAll = false;
	private boolean force = false;
	
	@TaskAction
	public void push() {
		PushCommand cmd = getGit().push();
		cmd.setCredentialsProvider(getCredentialsProvider());
		cmd.setRemote(getRemote());
		if (isPushTags()) {
			cmd.setPushTags();
		}
		if (isPushAll()) {
			cmd.setPushAll();
		}
		cmd.setForce(isForce());
		try {
			cmd.call();
		} catch (Exception e) {
			throw new GradleException("Problem pushing to repository.", e);
		}
		//TODO add progress monitor to go to Gradle status bar
		//TODO add support for credentials
	}
	
	@Input
	public PasswordCredentials getCredentials() {
		return credentials;
	}
	
	@SuppressWarnings("rawtypes")
	public void credentials(Closure closure) {
		ConfigureUtil.configure(closure, getCredentials());
	}
	
	public void setCredentials(PasswordCredentials credentials) {
		this.credentials = credentials;
	}
	
	private CredentialsProvider getCredentialsProvider() {
		if (getCredentials() == null
			|| ((getCredentials().getUsername() == null || getCredentials().getUsername().trim() == "")
			&& (getCredentials().getPassword() == null || getCredentials().getPassword().trim() == ""))) {
			return null;
		}
		return new UsernamePasswordCredentialsProvider(getCredentials().getUsername(), getCredentials().getPassword());
	}
	
	@Input
	public String getRemote() {
		return remote == null ? "origin" : ObjectUtil.unpackString(remote);
	}
	
	public void setRemote(Object remote) {
		this.remote = remote;
	}
	
	@Input
	public boolean isPushTags() {
		return pushTags;
	}
	
	public void setPushTags(boolean pushTags) {
		this.pushTags = pushTags;
	}
	
	@Input
	public boolean isPushAll() {
		return pushAll;
	}
	
	public void setPushAll(boolean pushAll) {
		this.pushAll = pushAll;
	}
	
	@Input
	public boolean isForce() {
		return force;
	}
	
	public void setForce(boolean force) {
		this.force = force;
	}
}
