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
package org.ajoberstar.gradle.git.auth;

import org.eclipse.jgit.awtui.AwtCredentialsProvider;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.gradle.api.artifacts.repositories.AuthenticationSupported;
import org.gradle.api.artifacts.repositories.PasswordCredentials;

/**
 * Provides support for creating a {@code JGitCredentialsProvider}.
 * @since 0.2.0
 */
public class JGitCredentialsProviderSupport {
	private final AuthenticationSupported authSupport;
	
	public JGitCredentialsProviderSupport(AuthenticationSupported authSupport) {
		this.authSupport = authSupport;
	}
	
	/**
	 * Gets a credentials provider.  If populated credentials
	 * are available, they will be used by the credentials
	 * provider.  If not, the credentials provider will
	 * prompt for any needed credentials. 
	 * @return a credentials provider
	 */
	public CredentialsProvider getCredentialsProvider() {
		PasswordCredentials creds = authSupport.getCredentials();
		if (creds != null && creds.getUsername() != null && creds.getPassword() != null) {
			return new UsernamePasswordCredentialsProvider(creds.getUsername(), creds.getPassword());
		}
		return new AwtCredentialsProvider();
	}
}
