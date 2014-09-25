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

import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TagStrategy {
	private static final Logger logger = LoggerFactory.getLogger(TagStrategy)

	boolean prefixNameWithV = true

	Closure generateMessage = { version -> "Release of ${version}" }


	String maybeCreateTag(ReleaseVersion version) {
		if (version.createTag) {
			String name = prefixNameWithV ? "v${version}" : version
			String message = generateMessage(version.version)

			logger.warn('Tagging repository as {}', name)
			grgit.tag.add(name: name, message: message)
			return name
		} else {
			return null
		}
	}
}
