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
package org.ajoberstar.gradle.git.ghpages

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.exception.GrgitException

import org.ajoberstar.gradle.git.auth.BasicPasswordCredentials
import org.ajoberstar.gradle.util.ObjectUtil

import org.gradle.api.Project
import org.gradle.api.artifacts.repositories.AuthenticationSupported
import org.gradle.api.artifacts.repositories.PasswordCredentials
import org.gradle.api.file.CopySpec
import org.gradle.util.ConfigureUtil

/**
 * Extension for gh-pages specific properties.
 * @since 0.1.0
 */
class GithubPagesPluginExtension implements AuthenticationSupported {
    private final Project project
    PasswordCredentials credentials = new BasicPasswordCredentials()

    /**
     * The URI of the Github repository.
     */
    Object repoUri

    /**
     * The branch of the Github repository to push to.
     * Defaults to {@code gh-pages}
     */
    Object targetBranch = 'gh-pages'

    /**
     * The distribution of files to put in gh-pages. Defaults
     * to including {@code src/main/ghpages}.
     */
    final CopySpec pages

    /**
     * The path to put the github repository in. Defaults to
     * {@code build/ghpages}.
     */
    Object workingPath = "${project.buildDir}/ghpages"

    /**
     * Whether to delete existing files in the branch, replacing the
     * entire contents. Defaults to {@code true}.
     */
    boolean deleteExistingFiles = true

    /**
     * The message used when committing changes to Github pages branch.
     * Defaults to 'Publish of Github pages from Gradle.'.
     */
    String commitMessage = 'Publish of Github pages from Gradle.'

    /**
     * Constructs the plugin extension.
     * @param project the project to create
     * the extension for
     */
    GithubPagesPluginExtension(Project project) {
        this.project = project
        this.pages = new DestinationCopySpec(project)
        pages.from 'src/main/ghpages'

        // defaulting the repoUri to the project repo's origin
        try {
            Grgit grgit = Grgit.open(currentDir: project.projectDir)
            this.repoUri = grgit.remote.list().find { it.name == 'origin' }?.url
            grgit.close()
        } catch (IllegalArgumentException e) {
            // there isn't a git repo
            this.repoUri = null
        } catch (GrgitException e) {
            // failed to open the repo of the current project
            this.repoUri = null
        }
    }

    /**
     * Gets the URI of the Github repository.  This
     * will be used to clone the repository.
     * @return the repo URI
     */
    String getRepoUri() {
        return ObjectUtil.unpackString(repoUri)
    }

    /**
     * Gets the working directory that the repo will be places in.
     * @return the working directory
     */
    File getWorkingDir() {
        return project.file(workingPath)
    }

    /**
     * Configures the gh-pages copy spec.
     * @param closure the configuration closure
     */
    void pages(Closure closure) {
        ConfigureUtil.configure(closure, pages)
    }

    /**
     * Configured the credentials to be used when interacting with
     * the repo. This will be passed a {@link PasswordCredentials}
     * instance.
     * @param closure the configuration closure
     */
    void credentials(Closure closure) {
        ConfigureUtil.configure(closure, credentials)
    }

    static class DestinationCopySpec implements CopySpec {
        private final Project project
        private Object destPath

        @Delegate
        CopySpec realSpec

        DestinationCopySpec(Project project) {
            this.project = project
            this.realSpec = project.copySpec {}
        }

        String getRelativeDestinationDir() {
            return destPath ?: '.'
        }

        @Override
        CopySpec into(Object destPath) {
            this.destPath = destPath
            realSpec.into(destPath)
            return this
        }
    }
}
