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
