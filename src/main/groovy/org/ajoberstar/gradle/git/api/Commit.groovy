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
package org.ajoberstar.gradle.git.api

import groovy.transform.Immutable

/**
 * Represents a Git commit.
 * @since 0.2.3
 * @author Andrew Oberstar
 */
 @Immutable
class Commit {
	/**
	 * The full hash of the commit.
	 */
    String id

    /**
     * The first 7 characters of the commit hash.
     */
    String abbreviatedId

    /**
     * The author of the changes in the commit.
     */
    Person author

    /**
     * The committer of the changes in the commit.
     */
    Person committer

    /**
     * The time the commit was created.
     */
    int time

    /**
     * The full commit message.
     */
    String fullMessage

    /**
     * The shortened commit message.
     */
    String shortMessage
}
