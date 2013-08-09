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
package org.ajoberstar.gradle.git.auth;

import org.eclipse.jgit.api.TransportCommand;
import org.eclipse.jgit.awtui.AwtCredentialsProvider;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.gradle.api.artifacts.repositories.AuthenticationSupported;
import org.gradle.api.artifacts.repositories.PasswordCredentials;

/**
 * Utility methods to configure a Git task that uses a TransportCommand
 * with authentication information.
 * @since 0.6.0
 */
public class TransportAuthUtil {
	private TransportAuthUtil() {
		throw new AssertionError("Cannot instantiate.");
	}

	/**
	 * Configures {@code command} to use the credentials from {@code auth}, if available,
	 * and also support use of ssh-agent and Pageant.
	 * @param command the transport command to configure
	 * @param auth the task with the credentials to use
	 */
	public static void configure(TransportCommand command, AuthenticationSupported auth) {
		command.setCredentialsProvider(createCredentialsProvider(auth));
		command.setTransportConfigCallback(new JschAgentProxyConfigCallback());
	}

	/**
	 * Creates a credentials provider based on the credentials from the given task. If both
	 * the username and password are provided by {@code auth}, those are used, otherwise
	 * will use an AWT window to prompt for any required credentials.
	 * @param auth the task with the credentials to use
	 */
	private static CredentialsProvider createCredentialsProvider(AuthenticationSupported auth) {
		PasswordCredentials creds = auth.getCredentials();
		if (creds != null && creds.getUsername() != null && creds.getPassword() != null) {
			return new UsernamePasswordCredentialsProvider(creds.getUsername(), creds.getPassword());
		}
		return new AwtCredentialsProvider();
	}
}
