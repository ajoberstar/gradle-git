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
package org.ajoberstar.gradle.git.release.experimental

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Experimental release plugin that removes some previous coupling to extensions.
 * Inteded to support semver-vcs, but may serve as a better minimal base.
 * @since 1.3.0
 */
class ExperimentalReleasePlugin implements Plugin<Project> {
    private static final Logger logger = LoggerFactory.getLogger(ExperimentalReleasePlugin)
    private static final String PREPARE_TASK_NAME = 'prepare'
    private static final String RELEASE_TASK_NAME = 'release'

    void apply(Project project) {
        project.plugins.apply('org.ajoberstar.grgit')
        addPrepareTask(project)
        addReleaseTask(project)
    }

    private void addPrepareTask(Project project) {
        project.tasks.create(PREPARE_TASK_NAME) {
            description = 'Verifies that the project could be released.'
            doLast {
                logger.info('Fetching changes from remote.')
                project.grgit.fetch()

                if (project.grgit.branch.status(branch: project.grgit.branch.current).behindCount > 0) {
                    throw new GradleException('Current branch is behind the tracked branch. Cannot release.')
                }
            }
        }

        project.tasks.all { task ->
            if (name != PREPARE_TASK_NAME) {
                task.shouldRunAfter PREPARE_TASK_NAME
            }
        }
    }

    private void addReleaseTask(Project project) {
        project.tasks.create(RELEASE_TASK_NAME) {
            description = 'Releases this project.'
            dependsOn PREPARE_TASK_NAME
            doLast {
                ext.toPush = [project.grgit.branch.current.fullName]

                // force version inference if it hasn't happened already
                ext.tagName = project.version.toString()
                if (tagName) {
                    toPush << tagName
                }

                logger.warn('Pushing changes in {} to remote.', toPush)
                project.grgit.push(refsOrSpecs: toPush)
            }
        }
    }
}
