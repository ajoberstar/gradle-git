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

import org.gradle.api.artifacts.repositories.AuthenticationSupported;
import org.gradle.api.artifacts.repositories.PasswordCredentials;

/**
 * Extension of {@code AuthenticationSupported} to allow
 * for configuration of credentials.
 * @since 0.2.0
 */
public interface ConfigurableAuthenticationSupported extends AuthenticationSupported {
	/**
	 * Sets the credentials to be used when interacting
	 * with the repo.
	 * @param credentials the credentials to use
	 */
	void setCredentials(PasswordCredentials credentials);
	
	/**
	 * Configured the credentials to be used when interacting
	 * with the repo. This will be passed a
	 * {@link PasswordCredentials} instance.
	 * @param closure the configuration closure
	 */
	@SuppressWarnings("rawtypes")
	void credentials(Closure closure);
}
