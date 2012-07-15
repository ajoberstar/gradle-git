package org.ajoberstar.gradle.util;

import groovy.lang.Closure;
import org.ajoberstar.gradle.git.plugins.BasicPasswordCredentials;
import org.eclipse.jgit.awtui.AwtCredentialsProvider;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.gradle.api.artifacts.repositories.PasswordCredentials;
import org.gradle.util.ConfigureUtil;

public class CredentialsEvaluator {
  private final ModifiableAuthenticationSupported authenticationSupported;

  public CredentialsEvaluator(ModifiableAuthenticationSupported authenticationSupported) {
    this.authenticationSupported = authenticationSupported;
  }

  public void evaluate(Closure closure) {
    if (authenticationSupported.getCredentials() == null) {
      authenticationSupported.setCredentials(new BasicPasswordCredentials());
    }
    ConfigureUtil.configure(closure, authenticationSupported.getCredentials());
  }

  public CredentialsProvider getCredentialsProvider() {
    PasswordCredentials creds = authenticationSupported.getCredentials();
    if (creds != null && creds.getUsername() != null && creds.getPassword() != null) {
      return new UsernamePasswordCredentialsProvider(creds.getUsername(), creds.getPassword());
    }
    return new AwtCredentialsProvider();
  }
}