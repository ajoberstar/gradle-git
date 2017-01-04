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

import groovy.transform.Immutable
import groovy.transform.PackageScope

import com.github.zafarkhaja.semver.Version
import org.ajoberstar.gradle.git.release.base.ReleasePluginExtension
import org.ajoberstar.gradle.git.release.base.ReleaseVersion
import org.ajoberstar.gradle.git.release.base.DefaultVersionStrategy
import org.ajoberstar.grgit.Grgit

import org.gradle.api.GradleException
import org.gradle.api.Project

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Strategy to infer versions that comply with Semantic Versioning.
 * @see PartialSemVerStrategy
 * @see SemVerStrategyState
 * @see <a href="https://github.com/ajoberstar/gradle-git/wiki/SemVer%20Support">Wiki Doc</a>
 */
@Immutable(copyWith=true, knownImmutableClasses=[PartialSemVerStrategy])
final class SemVerStrategy implements DefaultVersionStrategy {
    private static final Logger logger = LoggerFactory.getLogger(SemVerStrategy)
    static final String SCOPE_PROP = 'release.scope'
    static final String STAGE_PROP = 'release.stage'

    /**
     * The name of the strategy.
     */
    String name

    /**
     * The stages supported by this strategy.
     */
    SortedSet<String> stages

    /**
     * Whether or not this strategy can be used if the repo has uncommited changes.
     */
    boolean allowDirtyRepo

    /**
     * The strategy used to infer the normal component of the version. There is no enforcement that
     * this strategy only modify that part of the state.
     */
    PartialSemVerStrategy normalStrategy

    /**
     * The strategy used to infer the pre-release component of the version. There is no enforcement that
     * this strategy only modify that part of the state.
     */
    PartialSemVerStrategy preReleaseStrategy

    /**
     * The strategy used to infer the build metadata component of the version. There is no enforcement that
     * this strategy only modify that part of the state.
     */
    PartialSemVerStrategy buildMetadataStrategy

    /**
     * Whether or not to create tags for versions inferred by this strategy.
     */
    boolean createTag

    /**
     * Whether or not to enforce that versions inferred by this strategy are of higher precedence
     * than the nearest any.
     */
    boolean enforcePrecedence

    /**
     * Determines whether this strategy can be used to infer the version as a default.
     * <ul>
     * <li>Return {@code false}, if the {@code release.stage} is not one listed in the {@code stages} property.</li>
     * <li>Return {@code false}, if the repository has uncommitted changes and {@code allowDirtyRepo} is {@code false}.</li>
     * <li>Return {@code true}, otherwise.</li>
     * </ul>
     */
    @Override
    boolean defaultSelector(Project project, Grgit grgit) {
        String stage = getPropertyOrNull(project, STAGE_PROP)
        if (stage != null && !stages.contains(stage)) {
            logger.info('Skipping {} default strategy because stage ({}) is not one of: {}', name, stage, stages)
            return false
        } else if (!allowDirtyRepo && !grgit.status().clean) {
            logger.info('Skipping {} default strategy because repo is dirty.', name)
            return false
        } else {
            String status = grgit.status().clean ? 'clean' : 'dirty'
            logger.info('Using {} default strategy because repo is {} and no stage defined', name, status)
            return true
        }
    }

    /**
     * Determines whether this strategy should be used to infer the version.
     * <ul>
     * <li>Return {@code false}, if the {@code release.stage} is not one listed in the {@code stages} property.</li>
     * <li>Return {@code false}, if the repository has uncommitted changes and {@code allowDirtyRepo} is {@code false}.</li>
     * <li>Return {@code true}, otherwise.</li>
     * </ul>
     */
    @Override
    boolean selector(Project project, Grgit grgit) {
        String stage = getPropertyOrNull(project, STAGE_PROP)
        if (stage == null || !stages.contains(stage)) {
            logger.info('Skipping {} strategy because stage ({}) is not one of: {}', name, stage, stages)
            return false
        } else if (!allowDirtyRepo && !grgit.status().clean) {
            logger.info('Skipping {} strategy because repo is dirty.', name)
            return false
        } else {
            String status = grgit.status().clean ? 'clean' : 'dirty'
            logger.info('Using {} strategy because repo is {} and stage ({}) is one of: {}', name, status, stage, stages)
            return true
        }
    }

    /**
     * Infers the version to use for this build. Uses the normal, pre-release, and build metadata
     * strategies in order to infer the version. If the {@code release.stage} is not set, uses the
     * first value in the {@code stages} set (i.e. the one with the lowest precedence). After inferring
     * the version precedence will be enforced, if required by this strategy.
     */
    @Override
    ReleaseVersion infer(Project project, Grgit grgit) {
        def tagStrategy = project.extensions.getByType(ReleasePluginExtension).tagStrategy
        return doInfer(project, grgit, new NearestVersionLocator(tagStrategy))
    }

    @PackageScope
    ReleaseVersion doInfer(Project project, Grgit grgit, NearestVersionLocator locator) {
        ChangeScope scope = getPropertyOrNull(project, SCOPE_PROP).with { scope ->
            scope == null ? null : ChangeScope.valueOf(scope.toUpperCase())
        }
        String stage = getPropertyOrNull(project, STAGE_PROP) ?: stages.first()
        if (!stages.contains(stage)) {
            throw new GradleException("Stage ${stage} is not one of ${stages} allowed for strategy ${name}.")
        }
        logger.info('Beginning version inference using {} strategy and input scope ({}) and stage ({})', name, scope, stage)

        NearestVersion nearestVersion = locator.locate(grgit)
        logger.debug('Located nearest version: {}', nearestVersion)

        SemVerStrategyState state = new SemVerStrategyState(
            scopeFromProp: scope,
            stageFromProp: stage,
            currentHead: grgit.head(),
            currentBranch: grgit.branch.current,
            repoDirty: !grgit.status().clean,
            nearestVersion: nearestVersion
        )

        Version version = StrategyUtil.all(
            normalStrategy, preReleaseStrategy, buildMetadataStrategy).infer(state).toVersion()

        logger.warn('Inferred project: {}, version: {}', project.name, version)

        if (enforcePrecedence && version < nearestVersion.any) {
            throw new GradleException("Inferred version (${version}) cannot be lower than nearest (${nearestVersion.any}). Required by selected strategy.")
        }

        return new ReleaseVersion(version.toString(), nearestVersion.normal.toString(), createTag)
    }

    private String getPropertyOrNull(Project project, String name) {
        return project.hasProperty(name) ? project.property(name) : null
    }
}
