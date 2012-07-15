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

import groovy.lang.Closure;

import org.gradle.api.artifacts.repositories.PasswordCredentials;
import org.gradle.util.ConfigureUtil;

/**
 * Simple implementation of authentication support that
 * can be delegated to by classes that implement
 * {@code ConfigurableAuthenticationSupported}.
 * @since 0.2.0
 */
public class BasicAuthenticationSupport implements ConfigurableAuthenticationSupported {
	PasswordCredentials credentials = new BasicPasswordCredentials();
	
	/**
	 * {@inheritDoc
	 */
	@Override
	public PasswordCredentials getCredentials() {
		return credentials;
	}
	
	
	/**
	 * {@inheritDoc
	 */
	@Override
	public void setCredentials(PasswordCredentials credentials) {
		this.credentials = credentials;
	}

	/**
	 * {@inheritDoc
	 */
	@Override
	@SuppressWarnings("rawtypes")
	public void credentials(Closure closure) {
		if (credentials == null) {
			setCredentials(new BasicPasswordCredentials());
		}
		ConfigureUtil.configure(closure, getCredentials());
	}
}
