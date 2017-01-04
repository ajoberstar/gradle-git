/*
 * Copyright 2012-2017 the original author or authors.
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

import java.io.Serializable;

import org.ajoberstar.grgit.Credentials;

import org.gradle.api.artifacts.repositories.PasswordCredentials;

/**
 * Basic implementation of {@link PasswordCredentials}.
 * @since 0.1.0
 */
public class BasicPasswordCredentials implements PasswordCredentials, Serializable {
    private static final long serialVersionUID = 1L;
    private String username;
    private String password;

    /**
     * Constructs credentials with {@code null} username and password.
     */
    public BasicPasswordCredentials() {
        this(null, null);
    }

    /**
     * Constructs credentials with the given arguments.
     * @param username the username to set
     * @param password the password to set
     */
    public BasicPasswordCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Converts to credentials for use in Grgit.
     * @return {@code null} if both username and password are {@code null},
     * otherwise returns credentials in Grgit format.
     */
    public Credentials toGrgit() {
        if (username != null && password != null) {
            return new Credentials(username, password);
        } else {
            return null;
        }
    }
}
