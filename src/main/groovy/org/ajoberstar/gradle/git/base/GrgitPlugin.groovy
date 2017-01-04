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
package org.ajoberstar.gradle.git.base

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.exception.GrgitException
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin adding a {@code grgit} property to all projects
 * that searches for a Git repo from the root project's
 * directory.
 * @since 1.2.0
 */
class GrgitPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        try {
            Grgit grgit = Grgit.open(currentDir: project.rootProject.rootDir)
            project.rootProject.allprojects { prj ->
                project.ext.grgit = grgit
            }
        } catch (RepositoryNotFoundException | GrgitException ignored) {
            // not a git repo or invalid/corrupt
            project.logger.warn 'No git repository found. Build may fail with NPE.'
        }
    }
}
