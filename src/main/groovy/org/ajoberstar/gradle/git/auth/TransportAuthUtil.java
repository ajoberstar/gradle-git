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
