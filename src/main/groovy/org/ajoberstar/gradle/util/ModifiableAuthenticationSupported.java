package org.ajoberstar.gradle.util;

import org.gradle.api.artifacts.repositories.AuthenticationSupported;
import org.gradle.api.artifacts.repositories.PasswordCredentials;

public interface ModifiableAuthenticationSupported extends AuthenticationSupported {
   void setCredentials(PasswordCredentials credentials);
}