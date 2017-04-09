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
package org.ajoberstar.gradle.git.release.semver

import org.ajoberstar.gradle.git.release.base.ReleasePluginExtension
import org.ajoberstar.gradle.git.release.base.ReleaseVersion
import org.ajoberstar.gradle.git.release.base.VersionStrategy
import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Grgit

import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Strategy that infers the version based on the tag on the current
 * HEAD.
 */
class RebuildVersionStrategy implements VersionStrategy {
    private static final Logger logger = LoggerFactory.getLogger(RebuildVersionStrategy)
    public static final RebuildVersionStrategy INSTANCE = new RebuildVersionStrategy()

    private RebuildVersionStrategy() {
        // just hiding the constructor
    }

    @Override
    String getName() {
        return 'rebuild'
    }

    /**
     * Determines whether this strategy should be used to infer the version.
     * <ul>
     * <li>Return {@code false}, if any project properties starting with "release." are set.</li>
     * <li>Return {@code false}, if there aren't any tags on the current HEAD that can be parsed as a version.</li>
     * <li>Return {@code true}, otherwise.</li>
     * </ul>
     */
    @Override
    boolean selector(Project project, Grgit grgit) {
        def clean = grgit.status().clean
        def propsSpecified = project.hasProperty(SemVerStrategy.SCOPE_PROP) || project.hasProperty(SemVerStrategy.STAGE_PROP)
        def headVersion = getHeadVersion(project, grgit)

        if (clean && !propsSpecified && headVersion) {
            logger.info('Using {} strategy because repo is clean, no "release." properties found and head version is {}', name, headVersion)
            return true
        } else {
            logger.info('Skipping {} strategy because clean is {}, "release." properties are {} and head version is {}', name, clean, propsSpecified, headVersion)
            return false
        }
    }

    /**
     * Infers the version based on the version tag on the current HEAD with the
     * highest precendence.
     */
    @Override
    ReleaseVersion infer(Project project, Grgit grgit) {
        String version = getHeadVersion(project, grgit)
        def releaseVersion = new ReleaseVersion(version, version, false)
        logger.debug('Inferred version {} by strategy {}', releaseVersion, name)
        return releaseVersion
    }

    private String getHeadVersion(Project project, Grgit grgit) {
        def tagStrategy = project.extensions.getByType(ReleasePluginExtension).tagStrategy
        Commit head = grgit.head()
        return grgit.tag.list().findAll {
            it.commit == head
        }.collect {
            tagStrategy.parseTag(it)
        }.findAll {
            it != null
        }.max()?.toString()
    }
}
