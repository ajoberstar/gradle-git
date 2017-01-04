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
package org.ajoberstar.gradle.git.release.opinion

import org.ajoberstar.gradle.git.release.semver.RebuildVersionStrategy
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.exception.GrgitException

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin providing the base structure of gradle-git's flavor of release
 * behavior. The plugin can be applied using the {@code org.ajoberstar.release-base} id.
 *
 * <p>
 * The plugin applies the {@code org.ajoberstar.release-base} plugin and configures it to
 * use the following strategies (in order):
 * </p>
 *
 * <ul>
 *   <li>{@link RebuildVersionStrategy}</li>
 *   <li>{@link Strategies#DEVELOPMENT} (also set as the default)</li>
 *   <li>{@link Strategies#PRE_RELEASE}</li>
 *   <li>{@link Strategies#FINAL}</li>
 * </ul>
 *
 * <p>
 * Additionally it configures the tag strategy to generate a message from
 * the short messages of the commits since the previous version.
 * </p>
 *
 * @see org.ajoberstar.gradle.git.release.opinion.Strategies
 * @see org.ajoberstar.gradle.git.release.base.BaseReleasePlugin
 * @see <a href="https://github.com/ajoberstar/gradle-git/wiki/org.ajoberstar.release-opinion">Wiki Doc</a>
 */
class OpinionReleasePlugin implements Plugin<Project> {
    void apply(Project project) {
        project.plugins.apply('org.ajoberstar.release-base')

        project.release {
            versionStrategy RebuildVersionStrategy.INSTANCE
            versionStrategy Strategies.DEVELOPMENT
            versionStrategy Strategies.PRE_RELEASE
            versionStrategy Strategies.FINAL
            defaultVersionStrategy = Strategies.DEVELOPMENT
            tagStrategy {
                generateMessage = { version ->
                    StringBuilder builder = new StringBuilder()
                    builder << 'Release of '
                    builder << version.version
                    builder << '\n\n'

                    String previousVersion = "${project.release.tagStrategy.toTagString(version.previousVersion)}^{commit}"
                    List excludes = []
                    if (tagExists(grgit, previousVersion)) {
                        excludes << previousVersion
                    }
                    grgit.log(
                        includes: ['HEAD'],
                        excludes: excludes
                    ).inject(builder) { bldr, commit ->
                        bldr << '- '
                        bldr << commit.shortMessage
                        bldr << '\n'
                    }
                    builder.toString()
                }
            }
        }
    }

    private boolean tagExists(Grgit grgit, String revStr) {
        try {
            grgit.resolve.toCommit(revStr)
            return true
        } catch (GrgitException e) {
            return false
        }
    }
}
