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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.concurrent.Callable;

import org.ajoberstar.gradle.git.auth.BasicPasswordCredentials;
import org.ajoberstar.gradle.git.auth.TransportAuthUtil;
import org.ajoberstar.gradle.util.ObjectUtil;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.repositories.AuthenticationSupported;
import org.gradle.api.artifacts.repositories.PasswordCredentials;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.ConfigureUtil;

/**
 * Task to clone a Git repository. 
 * @since 0.1.0
 */
public class GitClone extends DefaultTask implements AuthenticationSupported {
	private static final String BRANCH_ROOT = "refs/heads/";
	private static final String TAG_ROOT = "refs/tags/";

	private PasswordCredentials credentials = new BasicPasswordCredentials();
	private Object uri = null;
	private Object remote = null;
	private boolean bare = false;
	private boolean checkout = true;
	private Object ref = null;
	private Collection<Object> branchesToClone = null;
	private boolean cloneAllBranches = true;
	private Object destinationPath = null;
	
	/**
	 * Clones a Git repository as configured.
	 */
	@TaskAction
	public void cloneRepo() {
		CloneCommand cmd = Git.cloneRepository();
		TransportAuthUtil.configure(cmd, this);
		cmd.setURI(getUri().toString());
		cmd.setRemote(getRemote());
		cmd.setBare(getBare());
		cmd.setNoCheckout(!getCheckout());
		cmd.setBranch(getRef());
		cmd.setBranchesToClone(getBranchesToClone());
		cmd.setCloneAllBranches(getCloneAllBranches());
		cmd.setDirectory(getDestinationDir());
		try {
			cmd.call();
		} catch (InvalidRemoteException e) {
			throw new GradleException("Invalid remote specified: " + getRemote(), e);
		} catch (TransportException e) {
			throw new GradleException("Problem with transport.", e);
		} catch (GitAPIException e) {
			throw new GradleException("Problem with clone.", e);
		}
		//TODO add progress monitor to log progress to Gradle status bar
	}

	/**
	 * Gets the credentials to be used when cloning the repo.
	 * @return the credentials
	 */
	@Input
	@Optional
	public PasswordCredentials getCredentials() {
		return credentials;
	}
	
	/**
	 * Configured the credentials to be used when cloning the repo.
	 * This will be passed a {@link PasswordCredentials} instance.
	 * @param closure the configuration closure
	 */
	@SuppressWarnings("rawtypes")
	public void credentials(Closure closure) {
		ConfigureUtil.configure(closure, getCredentials());
    }
	
	/**
	 * Sets the credentials to use when cloning the repo.
	 * @param credentials the credentials
	 */
	public void setCredentials(PasswordCredentials credentials) {
		this.credentials = credentials;
	}
	
	/**
	 * Gets the URI of the repo to clone.
	 * @return the uri
	 */
	@Input
	public String getUri() {
		return ObjectUtil.unpackString(uri);
	}
	
	/**
	 * Sets the URI of the repo to clone.
	 * @param uri the uri
	 */
	public void setUri(Object uri) {
		this.uri = uri;
	}
	
	/**
	 * Gets the name used to track the upstream repository.
	 * Defaults to "origin" if not set.
	 * @return the remote name
	 */
	@Input
	public String getRemote() {
		return remote == null ? "origin" : ObjectUtil.unpackString(remote);
    }
	
	/**
	 * Sets the name used to track the upstream repository.
	 * @param remote the remote name
	 */
	public void setRemote(Object remote) {
		this.remote = remote;
	}
	
	/**
	 * Gets whether the repository will be bare.
	 * @return whether the repo will be bare
	 */
	@Input
	public boolean getBare() {
		return bare;
	}
	
	/**
	 * Sets whether the repository will be bare.
	 * @param bare whether the repo will be bare
	 */
	public void setBare(boolean bare) {
		this.bare = bare;
	}
	
	/**
	 * Gets whether or not to checkout the specified branch.
	 * Defaults to {@code true}.
	 * @return whether or not to checkout the branch
	 */
	@Input
	public boolean getCheckout() {
		return checkout;
	}
	
	/**
	 * Sets whether or not to checkout the specified branch.
	 * @param checkout whether or not to checkout the branch
	 */
	public void setCheckout(boolean checkout) {
		this.checkout = checkout;
	}

	/**
	 * Gets the ref to checkout if {@code checkout} is set
	 * to {@code true}. Defaults to "refs/heads/master".
	 * @return the ref to checkout
	 * @since 0.5.0
	 */
	@Input
	public String getRef() {
		return ref == null ? BRANCH_ROOT + "master" : ObjectUtil.unpackString(ref);
	}

	/**
	 * Sets the ref to checkout if {@code checkout} is set
	 * to {@code true}.
	 * @param ref the ref to checkout
	 * @since 0.5.0
	 */
	public void setRef(Object ref) {
		this.ref = ref;
	}
	
	/**
	 * Gets the simple name of the branch to checkout if {@code checkout} is set
	 * to {@code true}.
	 * @return the branch to checkout or {@code null} if {@code ref} is not a branch
	 * @see #getRef()
	 */
	public String getBranch() {
		String ref = getRef();
		return ref.startsWith(BRANCH_ROOT) ? ref.substring(BRANCH_ROOT.length()) : null;
	}
	
	/**
	 * Sets the branch to checkout if {@code checkout} is set
	 * to {@code true}.
	 * @param branch the branch to checkout
	 * @see #setRef(Object)
	 */
	public void setBranch(final Object branch) {
		this.ref = new Callable<String>() {
			public String call() {
				return BRANCH_ROOT + ObjectUtil.unpackString(branch);
			}
		};
	}

	/**
	 * Gets the simple name of the tag to checkout if {@code checkout} is set
	 * to {@code true}.
	 * @return the tag to checkout or {@code null} if {@code ref} is not a tag
	 * @see #getRef()
	 * @since 0.5.0
	 */
	public String getTag() {
		String ref = getRef();
		return ref.startsWith(TAG_ROOT) ? ref.substring(TAG_ROOT.length()) : null;
	}
	
	/**
	 * Sets the tag to checkout if {@code checkout} is set
	 * to {@code true}. This will set the {@code ref}.
	 * @param tag the tag to checkout
	 * @see #setRef(Object)
	 * @since 0.5.0
	 */
	public void setTag(final Object tag) {
		this.ref = new Callable<String>() {
			public String call() {
				return TAG_ROOT + ObjectUtil.unpackString(tag);
			}
		};
	}	
	
	/**
	 * Gets the destination directory the repository
	 * will be cloned into.
	 * @return the path to clone into
	 */
	@OutputDirectory
	public File getDestinationDir() {
		return getProject().file(destinationPath);
	}
	
	/**
	 * Sets the path the repository should be clone into.
	 * Will be evaluated using {@link org.gradle.api.Project#file(Object)}.
	 * @param destinationPath the path to clone into
	 */
	public void setDestinationPath(Object destinationPath) {
		this.destinationPath = destinationPath;
	}
	
	/**
	 * Gets the branches to clone if {@code cloneAllBranches}
	 * is set to {@code false}.  If not set, it will default
	 * to {@code branch}.
	 * @return the branches to clone
	 */
	@Input
	public Collection<String> getBranchesToClone() {
		if (branchesToClone == null) {
			return Arrays.asList(getBranch());
		}
		Collection<String> branches = new HashSet<String>();
		for (Object branch : branchesToClone) {
			branches.add(ObjectUtil.unpackString(branch));
		}
		return branches;
	}
	
	/**
	 * Adds branches to clone if {@code cloneAllBranches}
	 * is set to {@code false}.
	 * @param branches the branches to clone
	 */
	public void branchesToClone(Object... branches) {
		if (branchesToClone == null) {
			this.branchesToClone = new ArrayList<Object>();
		}
		Collections.addAll(branchesToClone, branches);
	}
	
	/**
	 * Sets branches to clone if {@code cloneAllBranches}
	 * is set to {@code false}.
	 * @param branchesToClone the branches to clone
	 */
	@SuppressWarnings("unchecked")
	public void setBranchesToClone(Collection<? extends Object> branchesToClone) {
		this.branchesToClone = (Collection<Object>) branchesToClone;
		setCloneAllBranches(false);
	}
	
	/**
	 * Gets whether all branches should be cloned.
	 * @return whether all branches should be cloned
	 */
	@Input
	public boolean getCloneAllBranches() {
		return cloneAllBranches;
	}
	
	/**
	 * Sets whether all branches should be cloned.
	 * @param cloneAllBranches whether all branches
	 * should be cloned
	 */
	public void setCloneAllBranches(boolean cloneAllBranches) {
		this.cloneAllBranches = cloneAllBranches;
	}
}
