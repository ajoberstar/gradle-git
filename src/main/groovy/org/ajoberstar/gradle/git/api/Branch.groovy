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
package org.ajoberstar.gradle.git.api

import groovy.transform.Immutable

/**
 * Represents a Git branch.
 * @since 0.3.0
 * @author Andrew Oberstar
 */
 @Immutable
class Branch {
    /**
     * Gets the short name of the branch.
     */
    String name

    /**
     * Gets the full reference name of the branch.
     */
    String refName

    /**
     * Gets the commit the branch point to.
     */
    Commit commit
}
