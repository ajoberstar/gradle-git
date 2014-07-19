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
package org.ajoberstar.gradle.git.semver

import com.energizedwork.spock.extensions.TempDirectory
import org.ajoberstar.grgit.Grgit
import spock.lang.Shared
import spock.lang.Specification

import java.security.SecureRandom

class GrgitSpec extends Specification {
	@TempDirectory(clean=true)
	@Shared File repoDir

	@Shared Grgit grgit

	@Shared SecureRandom random = new SecureRandom()

	def setupSpec() {
		grgit = Grgit.init(dir: repoDir)
	}

	protected void commit() {
		byte[] bytes = new byte[128]
		random.nextBytes(bytes)
		new File(grgit.repository.rootDir, '1.txt') << bytes
		grgit.add(patterns: ['1.txt'])
		grgit.commit(message: 'do')
	}
}
