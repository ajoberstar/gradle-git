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
package org.ajoberstar.gradle.git.base

import org.ajoberstar.grgit.Grgit

/**
 * Extension giving access to a Grgit instance for the
 * repository.
 * @since 1.2.0
 */
class GrgitExtension {
	/**
	 * The repo directory that should be opened by getRepo().
	 */
	File dir

	/**
	 * Opens the repository indicated by dir.
	 */
	Grgit getRepo() {
		return Grgit.open(dir: dir)
	}
}
