/*
 * Copyright 2012-2015 the original author or authors.
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
import org.ajoberstar.grgit.Tag
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * Strategy for creating a Git tag associated with a release.
 */
class TagStrategy {
	/**
	 * The extension that was registered, ensures that we can use (and configure) the tagHandler defined.
	 */
	private final ReleasePluginExtension extension

	TagStrategy(ReleasePluginExtension extension) {
		this.extension = extension
	}

	private static final Logger logger = LoggerFactory.getLogger(TagStrategy)

	/**
	 * Added for backwards compatibility.
	 * @param prefix whether or not to prefix the tag with a 'v'
     */
	void setPrefixNameWithV(boolean prefix) {
		this.extension.tagHandler = TagHandler.Handlers.semver(prefix)
	}

	/**
	 * Closure taking a {@link ReleaseVersion} as an argument and returning
	 * a string to be used as the tag's message.
	 */
	Closure generateMessage = { version -> "Release of ${version.version}" }

	/**
	 * If the release version specifies a tag should be created, create a tag
	 * using the provided {@code Grgit} instance and this instance's state to
	 * determine the tag name and message.
	 * @paam grgit the repository to create the tag in
	 * @param version the version to create the tag for
	 * @return the name of the tag created, or {@code null} if it wasn't
	 */
	String maybeCreateTag(Grgit grgit, ReleaseVersion version) {
		if (version.createTag) {
			String name = this.extension.tagHandler.toTagString(version)
			String message = generateMessage(version)

			logger.warn('Tagging repository as {}', name)
			grgit.tag.add(name: name, message: message)
			return name
		} else {
			return null
		}
	}
}
