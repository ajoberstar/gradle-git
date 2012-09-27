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

import java.io.IOException;

import org.ajoberstar.gradle.util.ObjectUtil;
import org.eclipse.jgit.api.MergeCommand;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.merge.MergeStrategy;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

/**
 * Task to join two or more development histories together.
 * 
 * @since 0.2.1
 * @author Alex Lixandru
 */
public class GitMerge extends GitBase {
	private Object strategy = null;
	private Object ref = null;
	
	/**
	 * Merges some changes with the current branch.
	 */
	@TaskAction
	public void merge() {
		MergeCommand cmd = getGit().merge();
		cmd.include(findRef());
		cmd.setStrategy(getMergeStrategy());
		try {
			cmd.call();
		} catch (CheckoutConflictException e) {
			throw new GradleException("The working tree changes conflict with the specified commit.", e);
		} catch (GitAPIException e) {
			throw new GradleException("Problem with merge.", e);
		}		
		//TODO add progress monitor to log progress to Gradle status bar
	}

	/**
	 * Gets the ref that will be merges with the current branch.
	 * @return the ref which will be merged with the current branch
	 */
	@Input
	public String getRef() {
		if( ref == null ) {
			return null;
		} else {
			return ObjectUtil.unpackString(ref);
		}
	}
	
	/**
	 * Sets the ref that will be merged with the current branch.
	 * @param ref the ref that will be merged
	 */
	public void setRef(Object ref) {
		this.ref = ref;
	}
	
	/**
	 * Gets the merge strategy.
	 * @return the merge strategy; either {@code resolve}, {@code ours}, 
	 *         {@code theirs} or {@code simple_two_way_in_core}
	 */
	@Input
	@Optional
	public Object getStrategy() {
		return strategy;
	}

	/**
	 * Sets the merge strategy
	 * @param  mode the merge strategy. Must be
	 *         one of the following values: <ul>
	 *         <li>{@code resolve} 
	 *         <li>{@code ours} 
	 *         <li>{@code theirs} 
	 *         <li>{@code simple_two_way_in_core}
	 *         </ul>
	 */
	public void setStrategy(Object mode) {
		this.strategy = mode;
	}
	
	/**
	 * Attempts to get a valid {@link MergeStrategy} out of the user
	 * configuration
	 * 
	 * @return the merge strategy corresponding to the user input 
	 */
	private MergeStrategy getMergeStrategy() {
		String modeConfig = ObjectUtil.unpackString(getStrategy());
		if( modeConfig == null ) {
			return MergeStrategy.RESOLVE;
		} else {
			try {
				return MergeStrategy.get(modeConfig.toUpperCase());
			} catch (Exception e) {
				throw new GradleException("No valid merge strategy could be " +
					"identified from the specified input: " + modeConfig, e);
			}
		}
	}
	
	/**
	 * Attempts to find a valid commit from the ref the user passed
	 * to the task configuration.
	 * 
	 * @return the ID of the commit corresponding to the user-specified ref
	 */
	private ObjectId findRef() {
		String ref = getRef();
		
		if( ref == null ) {
			throw new GradleException("Invalid ref specified");
		}
		
		try {
			final ObjectId commitId = getGit().getRepository().resolve(ref + "^{commit}");
			if (commitId == null) {
				throw new GradleException("No valid commit could be identified " +
					"from the specified ref: " + ref);
			}
			return commitId;
		} catch (IOException e) {
			throw new GradleException("Unable to identify a commit " +
				"from the specified ref: " + ref, e);
		}
	}
}
