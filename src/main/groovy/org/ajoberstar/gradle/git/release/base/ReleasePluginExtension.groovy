/*
 * Copyright 2012-2014 the original author or authors.
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

import org.ajoberstar.gradle.git.release.base.ReleaseVersion
import org.ajoberstar.gradle.git.release.base.TagStrategy
import org.ajoberstar.gradle.git.release.base.VersionStrategy
import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.util.ConfigureUtil

import org.gradle.api.GradleException
import org.gradle.api.Project

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class ReleasePluginExtension {
	private static final Logger logger = LoggerFactory.getLogger(ReleasePluginExtension)
	protected final Project project
	private final Map<String, VersionStrategy> versionStrategies = [:]
	final TagStrategy tagStrategy = new TagStrategy()

	VersionStrategy defaultVersionStrategy

	Grgit grgit

	String remote = 'origin'

	ReleasePluginExtension(Project project) {
		this.project = project
		project.version = new DelayedVersion()
	}

	List<VersionStrategy> getVersionStrategies() {
		return versionStrategies.collect { key, value -> value }.asImmutable()
	}

	void versionStrategy(VersionStrategy strategy) {
		versionStrategies[strategy.name] = strategy
	}

	void tagStrategy(Closure closure) {
		ConfigureUtil.configure(tagStrategy, closure)
	}

	// TODO: Decide if this should be thread-safe.
	private class DelayedVersion {
		ReleaseVersion inferredVersion

		private void infer() {
			// TODO: Should this happen every time?
			logger.info('Fetching changes from remote: {}', remote)
			grgit.fetch(remote: remote)

			VersionStrategy selectedStrategy = versionStrategies.find { strategy ->
				strategy.selector(project, grgit)
			}

			if (!selectedStrategy) {
				if (defaultVersionStrategy) {
					logger.info('Falling back to default strategy: {}', defaultVersionStrategy.name)
					selectedStrategy = defaultVersionStrategy
				} else {
					throw new GradleException('No strategies were selected and defaultVersionStrategy is not set.')
				}
			}

			inferredVersion = selectedStrategy.infer(project, grgit)
		}

		@Override
		public String toString() {
			if (!inferredVersion) {
				infer()
			}
			return inferredVersion.version
		}
	}
}
