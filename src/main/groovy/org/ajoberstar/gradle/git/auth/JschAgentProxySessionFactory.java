package org.ajoberstar.gradle.git.auth;

import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.util.FS;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.agentproxy.AgentProxyException;
import com.jcraft.jsch.agentproxy.Connector;
import com.jcraft.jsch.agentproxy.RemoteIdentityRepository;
import com.jcraft.jsch.agentproxy.USocketFactory;
import com.jcraft.jsch.agentproxy.connector.PageantConnector;
import com.jcraft.jsch.agentproxy.connector.SSHAgentConnector;
import com.jcraft.jsch.agentproxy.usocket.JNAUSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JschAgentProxySessionFactory extends JschConfigSessionFactory {
	private static final Logger logger = LoggerFactory.getLogger(JschAgentProxySessionFactory.class);

	protected void configure(Host hc, Session session) {
		// no action
	}

	protected JSch getJsch(Host hc, FS fs) {
		JSch jsch = getJsch(hc, fs);
		Connector con = determineConnector();
		if (con != null) {
			jsch.setIdentityRepository(new RemoteIdentityRepository(con));
		}
		return jsch;
	}

	private Connector determineConnector() {
		try {
			if (SSHAgentConnector.isConnectorAvailable()) {
				USocketFactory usf = new JNAUSocketFactory();
				return new SSHAgentConnector(usf);
			} else if (PageantConnector.isConnectorAvailable()) {
				return new PageantConnector();
			} else {
				return null;
			}
		} catch (AgentProxyException e) {
			logger.debug("Could not configure JSCH agent proxy connector.", e);
			return null;
		}
	}
}
