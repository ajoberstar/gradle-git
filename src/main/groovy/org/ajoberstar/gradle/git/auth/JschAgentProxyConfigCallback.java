package org.ajoberstar.gradle.git.auth;

import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.transport.SshTransport;
import org.eclipse.jgit.transport.Transport;

public class JschAgentProxyConfigCallback implements TransportConfigCallback {
	public void configure(Transport transport) {
		if (transport instanceof SshTransport) {
			SshTransport sshTransport = (SshTransport) transport;
			sshTransport.setSshSessionFactory(new JschAgentProxySessionFactory());
		}
	}
}
