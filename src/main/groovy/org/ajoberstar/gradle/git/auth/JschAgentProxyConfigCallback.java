package org.ajoberstar.gradle.git.auth;

import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;

/**
 * Callback to configure a Transport to support JSch agent proxying. This
 * allows use of agents, such as ssh-agent and Pageant, to provide SSH
 * authentication.
 * @since 0.6.0
 */
public class JschAgentProxyConfigCallback implements TransportConfigCallback {
	/**
	 * Configures the {@code transport} to support JSch agent proxy,
	 * if it is an SSH transport.
	 * @param transport the transport to configure
	 */
	public void configure(Transport transport) {
		if (transport instanceof SshTransport) {
			SshTransport sshTransport = (SshTransport) transport;
			sshTransport.setSshSessionFactory(new JschAgentProxySessionFactory());
		}
	}
}
