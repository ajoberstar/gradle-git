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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.ajoberstar.gradle.git.auth.BasicPasswordCredentials;
import org.ajoberstar.gradle.git.auth.JGitCredentialsProviderSupport;
import org.ajoberstar.gradle.util.ObjectUtil;
import org.eclipse.jgit.api.FetchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.TagOpt;
import org.gradle.api.GradleException;
import org.gradle.api.artifacts.repositories.AuthenticationSupported;
import org.gradle.api.artifacts.repositories.PasswordCredentials;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.TaskAction;
import org.gradle.util.ConfigureUtil;

/**
 * Task to fetch named heads or tags from one or more other repositories, along 
 * with the objects necessary to complete them.
 *  
 * @since 0.2.1
 * @author Alex Lixandru
 */
public class GitFetch extends GitBase implements AuthenticationSupported {
    private PasswordCredentials credentials = new BasicPasswordCredentials();
    private JGitCredentialsProviderSupport credsProviderSupport = new JGitCredentialsProviderSupport(this);
    
    private boolean dryRun = false;
    private boolean thin = true;
    private boolean remRefs = false;
    private boolean checkObjects = false;
    private Object remote = null;
    private Object fetchTags = null;
    private Collection<Object> refsToFetch = null;
    
    /**
     * Fetches remote changes into a local Git repository.
     */
    @TaskAction
    public void fetch() {
        FetchCommand cmd = getGit().fetch();
        cmd.setCredentialsProvider(credsProviderSupport.getCredentialsProvider());
        cmd.setTagOpt(getTagOpt());
        cmd.setCheckFetchedObjects(getCheckFetchedObjects());
        cmd.setDryRun(getDryRun());
        cmd.setRefSpecs(getRefspecs());
        cmd.setRemote(getRemote());
        cmd.setRemoveDeletedRefs(getRemoveDeletedRefs());
        cmd.setThin(getThin());
        try {
            cmd.call();
        } catch (InvalidRemoteException e) {
            throw new GradleException("Invalid remote specified: " + getRemote(), e);
        } catch (TransportException e) {
            throw new GradleException("Problem with transport.", e);
        } catch (GitAPIException e) {
            throw new GradleException("Problem with fetch.", e);
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
     * Gets the name used to track the upstream repository.
     * Defaults to {@code origin} if not set.
     * @return the remote name
     */
    @Input
    public String getRemote() {
        return remote == null ? "origin" : ObjectUtil.unpackString(remote);
    }
    
    /**
     * Sets the name used to track the upstream repository.
     * Defaults to {@code origin} if not set.
     * @param remote the remote name
     */
    public void setRemote(Object remote) {
        this.remote = remote;
    }
    
    
    /**
     * Gets whether the fetch operation will be a dry run
     * @return whether the fetch operation will be a dry run
     */
    @Input
    public boolean getDryRun() {
        return dryRun;
    }
    
    /**
     * Sets whether the fetch operation should be a dry run. 
     * Defaults to false.
     * @param dryRun whether the fetch operation should be a dry run
     */
    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }
    
    /**
     * Gets the thin-pack preference for fetch operation
     * @return true if a thin-pack mode will be used for transport or false otherwise
     */
    @Input
    public boolean getThin() {
        return thin;
    }
    
    /**
     * Sets the thin-pack preference for fetch operation. Default setting 
     * is {@code true} (thin transport)
     * @param thin whether the transport preference will be {@code thin}
     */
    public void setThin(boolean thin) {
        this.thin = thin;
    }
    
    /**
     * Gets whether refs which no longer exist in the source will be removed
     * @return whether refs which no longer exist in the source will be removed
     */
    @Input
    public boolean getRemoveDeletedRefs() {
        return remRefs;
    }
    
    /**
     * If set to true, refs which no longer exist in the source will be removed.
     * Defaults to false.
     * @param remRefs whether refs which no longer exist should be removed
     */
    public void setRemoveDeletedRefs(boolean remRefs) {
        this.remRefs = remRefs;
    }
    
    /**
     * Gets whether received objects will be checked for validity.
     * @return whether objects will be checked for validity
     */
    @Input
    public boolean getCheckFetchedObjects() {
        return checkObjects;
    }
    
    /**
     * If set to true, the received objects will be checked for validity.
     * Defaults to false.
     * @param checkObjects whether to check objects for validity
     */
    public void setCheckFetchedObjects(boolean checkObjects) {
        this.checkObjects = checkObjects;
    }
    
    /**
     * Gets the refspecs of the fetch command.
     * @return the refspecs to fetch
     */
    @Input
    @Optional
    public List<RefSpec> getRefspecs() {
        if (refsToFetch == null) {
            return Collections.emptyList();
        }
        List<RefSpec> refspecs = new ArrayList<RefSpec>();
        for (Object branch : refsToFetch) {
            try {
                refspecs.add(new RefSpec(ObjectUtil.unpackString(branch)));
            } catch( IllegalArgumentException e ) {
                throw new GradleException("Invalid refs specified", e);
            }
        }
        return refspecs;
    }
    
    /**
     * Adds refspecs to fetch from the remote repository.
     * 
     * <p>The format of a {@code refspec} parameter is an optional 
     * plus +, followed by the source ref {@code src}, followed by 
     * a colon {@code :}, followed by the destination ref {@code dst}.</p>
     * 
     * <p>The remote ref that matches {@code src} is fetched, and if 
     * {@code dst} is not empty string, the local ref that matches it
     * is fast-forwarded using {@code src}. If the optional plus + is 
     * used, the local ref is updated even if it does not result in a 
     * fast-forward update.</p>
     * 
     * If no refspec is specified, all the remote-tracking branches 
     * will be fetched.
     * 
     * @param refs the refspecs to fetch
     */
    public void refspecs(Object... refs) {
        if (refsToFetch == null) {
            this.refsToFetch = new ArrayList<Object>();
        }
        Collections.addAll(refsToFetch, refs);
    }
    
    /**
     * Sets refspecs for the fetch command.
     * 
     * <p>The format of a {@code refspec} parameter is an optional 
     * plus +, followed by the source ref {@code src}, followed by 
     * a colon {@code :}, followed by the destination ref {@code dst}.</p>
     * 
     * <p>The remote ref that matches {@code src} is fetched, and if 
     * {@code dst} is not empty string, the local ref that matches it
     * is fast-forwarded using {@code src}. If the optional plus + is 
     * used, the local ref is updated even if it does not result in a 
     * fast-forward update.</p>
     * 
     * If no refspec is specified, all the remote-tracking branches 
     * will be fetched.
     * 
     * @param refsToFetch the refspecs to fetch
     */
    @SuppressWarnings("unchecked")
    public void setRefspecs(List<? extends Object> refsToFetch) {
        this.refsToFetch = (List<Object>) refsToFetch;
    }
    
    /**
     * Gets whether tags will be fetched.
     * @return the fetch mode for tags; either {@code yes}, {@code no} or 
     *         {@code auto}
     */
    @Input
    @Optional
    public Object getFetchTags() {
        return fetchTags;
    }

    /**
     * Sets the specification of annotated tag behavior during fetch. 
     * Default mode is {@code auto}, which means tags are automatically 
     * followed if they point at a commit that is being fetched.
     * 
     * @param  fetchTags the specification of tag behavior during 
     *         fetch. Must be one of the following values: <ul>
     *          <li>{@code yes}, if tags should be fetched, 
     *          <li>{@code no}, if tags should not be fetched or 
     *          <li>{@code auto}, for the default behavior
     *          </ul>
     */
    public void setFetchTags(Object fetchTags) {
        this.fetchTags = fetchTags;
    }

    /**
     * Attempts to get a valid {@link TagOpt} out of the user
     * configuration
     * 
     * @return the TagOpt corresponding to the user input 
     */
    private TagOpt getTagOpt() {
        Object tagConfig = getFetchTags();
        
        if( tagConfig == null ) {
            return TagOpt.AUTO_FOLLOW;
        }
        
        if( tagConfig instanceof String ) {
            String tc = ((String) tagConfig);
            if( tc.equalsIgnoreCase("auto") ) {
                return TagOpt.AUTO_FOLLOW;
            } else if( tc.equalsIgnoreCase("yes") ) {
                return TagOpt.FETCH_TAGS;
            } else if( tc.equalsIgnoreCase("no") ) {
                return TagOpt.NO_TAGS;
            } else {
                throw new GradleException("No valid tag option could be " +
                		"identified from the specified input: " + tc);
            }
        } else {
            throw new GradleException("No valid tag option could be " +
            		"identified");
        }
    }
}
