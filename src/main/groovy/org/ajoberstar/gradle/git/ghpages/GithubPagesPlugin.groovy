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
import org.ajoberstar.grgit.operation.ResetOp
import org.ajoberstar.grgit.exception.GrgitException
import org.eclipse.jgit.errors.RepositoryNotFoundException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.Copy

/**
 * Plugin to enable publishing to gh-pages branch of Github.
 * @since 0.1.0
 */
class GithubPagesPlugin implements Plugin<Project> {
    static final String PREPARE_TASK_NAME = 'prepareGhPages'
    static final String PUBLISH_TASK_NAME = 'publishGhPages'

    /**
     * Applies the plugin to the given project.
     * @param project the project
     */
    void apply(Project project) {
        project.logger.warn('org.ajoberstar.github-pages is deprecated will be removed in gradle-git 2.0.0. Users should migrate to org.ajoberstar.git-publish (https://github.com/ajoberstar/gradle-git-publish).')
        GithubPagesPluginExtension extension = project.extensions.create('githubPages', GithubPagesPluginExtension, project)
        configureTasks(project, extension)
    }

    /**
     * Configures the tasks to publish to gh-pages.
     * @param project the project to configure
     * @param extension the plugin extension
     */
    private void configureTasks(final Project project, final GithubPagesPluginExtension extension) {
        Task prepare = createPrepareTask(project, extension)
        Task publish = createPublishTask(project, extension)
        publish.dependsOn(prepare)
    }

    private Task createPrepareTask(Project project, GithubPagesPluginExtension extension) {
        Task task = project.tasks.create(PREPARE_TASK_NAME, Copy)
        task.with {
            description = 'Prepare the gh-pages changes locally'
            with extension.pages.realSpec
            into { extension.workingDir }
            doFirst {
                def repo = repo(project, extension)
                if (extension.deleteExistingFiles) {
                    def relDestDir = extension.pages.relativeDestinationDir
                    def targetDir = new File(extension.workingDir, relDestDir)
                    def filesList = targetDir.list { dir, name -> !name.equals('.git') }
                    if(filesList) {
                        def removePatterns = filesList
                        if(relDestDir && relDestDir != '.') {
                            removePatterns = filesList.collect {name -> "$relDestDir/$name"}
                        }
                        repo.remove(patterns: removePatterns)
                    }
                }
            }
            doLast {
                def repo = repo(project, extension)
                repo.with {
                    add(patterns: ['.'])
                    if (status().clean) {
                        project.logger.warn 'Nothing to commit, skipping publish.'
                    } else {
                        commit(message: extension.commitMessage)
                    }
                }
            }
        }
        return task
    }

    private Task createPublishTask(Project project, GithubPagesPluginExtension extension) {
        return project.tasks.create(PUBLISH_TASK_NAME) {
            description = 'Publishes all gh-pages changes to Github'
            group = 'publishing'
            // only push if there are commits to push
            onlyIf {
                def repo = repo(project, extension)
                def status = repo.branch.status(name: repo.branch.current)
                status.aheadCount > 0
            }
            doLast {
                repo(project, extension).push()
            }
        }
    }

    private Grgit repo(Project project, GithubPagesPluginExtension extension) {
        if (extension.ext.has('repo')) {
            return extension.ext.repo
        }
        def repo = null
        try {
            // attempt to reuse existing repository
            repo = Grgit.open(dir: extension.workingDir)
            if (extension.repoUri == repo.remote.list().find { it.name == 'origin' }?.url &&
                    repo.branch.current.name == extension.targetBranch) {
                repo.clean(directories: true, ignore: false)
                repo.fetch()
                repo.reset(commit: 'origin/' + extension.targetBranch, mode: ResetOp.Mode.HARD)
            }
            else {
                project.logger.warn('Found a git repository at workingDir, but it does not match configuration. A fresh clone will be used.')
                repo.close()
                repo = null
            }
        } catch (RepositoryNotFoundException ignored) {
            // not a git repo
        } catch (GrgitException ignored) {
            // invalid/corrup git repo
        }

        if (!repo) {
            extension.workingDir.deleteDir()
            repo = Grgit.clone(
                uri: extension.repoUri,
                refToCheckout: extension.targetBranch,
                dir: extension.workingDir,
                credentials: extension.credentials?.toGrgit()
            )

            // check if on the correct branch, which implies it doesn't exist
            if (repo.branch.current.name != extension.targetBranch) {
                repo.checkout(branch: extension.targetBranch, orphan: true)
                // need to wipe out the current files
                extension.deleteExistingFiles = true
            }
        }
        extension.ext.repo = repo
        return repo
    }
}
