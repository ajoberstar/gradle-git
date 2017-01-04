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
package org.ajoberstar.gradle.git.release.base

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.util.ConfigureUtil

import org.gradle.api.GradleException
import org.gradle.api.Project

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Extension providing configuration options for gradle-git's release plugins.
 *
 * <p>
 * Sets the version to a {@link DelayedVersion} which will infer the version
 * when {@code toString()} is called on it. A strategy will be selected from the
 * ones configured on this extension and then used to infer the version.
 * </p>
 *
 * @see org.ajoberstar.gradle.git.release.base.BaseReleasePlugin
 * @see org.ajoberstar.gradle.git.release.opinion.OpinionReleasePlugin
 */
class ReleasePluginExtension {
    private static final Logger logger = LoggerFactory.getLogger(ReleasePluginExtension)
    protected final Project project
    private final Map<String, VersionStrategy> versionStrategies = [:]

    /**
     * The strategy to use when creating a tag for the inferred version.
     */
    final TagStrategy tagStrategy = new TagStrategy()

    /**
     * The strategy to use if all of the ones in {@code versionStrategies} return
     * false from their {@code selector()} methods. This strategy can be, but is
     * not required to be, one from {@code versionStrategies}.
     */
    VersionStrategy defaultVersionStrategy

    /**
     * The repository to infer the version from.
     */
    Grgit grgit

    /**
     * The remote to fetch changes from and push changes to.
     */
    String remote = 'origin'

    ReleasePluginExtension(Project project) {
        this.project = project
        def sharedVersion = new DelayedVersion()
        project.rootProject.allprojects {
            version = sharedVersion
        }
    }

    /**
     * Gets all strategies in the order they were inserted into the extension.
     */
    List<VersionStrategy> getVersionStrategies() {
        return versionStrategies.collect { key, value -> value }.asImmutable()
    }

    /**
     * Adds a strategy to the extension. If the strategy has the same name as
     * one already configured, it will replace the existing one.
     */
    void versionStrategy(VersionStrategy strategy) {
        versionStrategies[strategy.name] = strategy
    }

    /**
     * Configures the tag strategy with the provided closure.
     */
    void tagStrategy(Closure closure) {
        ConfigureUtil.configure(tagStrategy, closure)
    }

    // TODO: Decide if this should be thread-safe.
    private class DelayedVersion {
        ReleaseVersion inferredVersion

        private void infer() {
            VersionStrategy selectedStrategy = versionStrategies.find { strategy ->
                strategy.selector(project, grgit)
            }

            if (!selectedStrategy) {
                boolean useDefault
                if (defaultVersionStrategy instanceof DefaultVersionStrategy) {
                    useDefault = defaultVersionStrategy.defaultSelector(project, grgit)
                } else {
                    useDefault = defaultVersionStrategy?.selector(project, grgit)
                }

                if (useDefault) {
                    logger.info('Falling back to default strategy: {}', defaultVersionStrategy.name)
                    selectedStrategy = defaultVersionStrategy
                } else {
                    throw new GradleException('No version strategies were selected. Run build with --info for more detail.')
                }
            }

            inferredVersion = selectedStrategy.infer(project, grgit)
        }

        @Override
        String toString() {
            if (!inferredVersion) {
                infer()
            }
            return inferredVersion.version
        }
    }
}
