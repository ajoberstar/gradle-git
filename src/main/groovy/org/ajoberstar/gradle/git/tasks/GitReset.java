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
import java.util.Collections;
import java.util.List;

import org.ajoberstar.gradle.util.ObjectUtil;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.ResetCommand.ResetType;
import org.eclipse.jgit.api.errors.CheckoutConflictException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;

/**
 * Task to reset the current HEAD of a local Git repository to the specified 
 * state.
 * 
 * @since 0.2.1
 * @author Alex Lixandru
 */
public class GitReset extends GitBase {
	private Object mode = null;
	private Object ref = null;
	private List<Object> paths = null;
	
	/**
	 * Reset the changes as configured.
	 */
	@TaskAction
	public void reset() {
	    ResetCommand cmd = getGit().reset();
	    cmd.setRef(getRef());
	    cmd.setMode(getResetType());
	    
	    List<String> pathsToReset = getPaths();
	    for (String path : pathsToReset) {
            cmd.addPath(path);
        }
	    
		try {
		    cmd.call();
		} catch (CheckoutConflictException e) {
			throw new GradleException("The working tree changes conflict with the specified commit.", e);
		} catch (GitAPIException e) {
		    throw new GradleException("Problem with reset.", e);
		}		
		//TODO add progress monitor to log progress to Gradle status bar
	}

	/**
     * Gets the ref to reset. If not set, it will default 
     * to {@code HEAD}.
     * @return the branch to fetch
     */
    @Input
    public String getRef() {
        return ref == null ? "HEAD" : ObjectUtil.unpackString(ref);
    }
    
    /**
     * Sets the ref to reset. Defaults to {@code HEAD}.
     * @param ref the ref to reset
     */
    public void setRef(Object ref) {
        this.ref = ref;
    }
    
    /**
     * Gets the paths to reset.
     * @return the paths to reset
     */
    @Input
    public List<String> getPaths() {
        if (paths == null) {
            return Collections.emptyList();
        }
        List<String> resetPaths = new ArrayList<String>();
        for (Object path : paths) {
            resetPaths.add(ObjectUtil.unpackString(path));
        }
        return resetPaths;
    }
    
    /**
     * Adds paths to be reset.
     * 
     * @param resetPaths the paths to be reset
     */
    public void paths(Object... resetPaths) {
        if (paths == null) {
            this.paths = new ArrayList<Object>();
        }
        Collections.addAll(paths, resetPaths);
    }
    
    /**
     * Sets the paths for the reset command.
     * 
     * <p>As per Git's {@code reset} command manual, no path should be added 
     * if the {@code mode} property has been set.</p>
     * @param resetPaths the paths to be reset
     */
    @SuppressWarnings("unchecked")
    public void setPaths(List<? extends Object> resetPaths) {
        this.paths = (List<Object>) resetPaths;
    }
    
    
    /**
     * Gets the reset mode.
     * @return the reset mode; either {@code soft}, {@code mixed}, 
     *         {@code hard}, {@code merge} or {@code keep}
     */
    @Input
    @Optional
    public Object getMode() {
        return mode;
    }

    /**
     * Sets the reset mode
     * 
     * @param  mode the reset mode specification. Must be
     *          one of the following values: <ul>
     *          <li>{@code soft}, for soft reset, 
     *          <li>{@code mixed}, for mixed reset 
     *          <li>{@code hard}, for hard reset 
     *          <li>{@code merge}, for merge reset
     *          <li>{@code keep}, for keep reset
     *          </ul>
     */
    public void setMode(Object mode) {
        this.mode = mode;
    }
	
    /**
     * Attempts to get a valid {@link ResetType} out of the user
     * configuration.
     * 
     * <p>This will throw a {@link GradleException} if no mode is
     * specified and no paths have been set for reset, since the
     * reset command cannot continue.</p>
     * 
     * @return the ResetType corresponding to the user input 
     */
    private ResetType getResetType() {
        Object modeConfig = getMode();
        
        if( modeConfig == null ) {
            if( paths == null || paths.size() == 0 ) {
                throw new GradleException("No reset mode and no file path " +
                		"has been specified. One of the them is needed to " +
                		"complete the reset");
            }
            return null;
        }
        
        if( modeConfig instanceof String ) {
            String rt = ((String) modeConfig).toUpperCase();
            try {
                return ResetType.valueOf(rt);
            } catch (Exception e) {
                throw new GradleException("No valid reset mode could be " +
                		"identified from the specified input: " + rt, e);
            }
            
        } else {
            throw new GradleException("No valid reset mode could be identified");
        }
    }
}
