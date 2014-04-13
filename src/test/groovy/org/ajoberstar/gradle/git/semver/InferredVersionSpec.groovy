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

import java.security.SecureRandom

import com.energizedwork.spock.extensions.TempDirectory

import org.ajoberstar.grgit.Grgit

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class InferredVersionSpec extends Specification {
	@TempDirectory(clean=true)
	@Shared File repoDir

	@Shared Grgit grgit

	@Shared SecureRandom random = new SecureRandom()

	def setupSpec() {
		grgit = Grgit.init(dir: repoDir)

		commit()
		commit()
		grgit.branch.add(name: 'unreachable')

		commit()
		grgit.tag.add(name: '0.0.1-milestone.3')

		commit()
		grgit.branch.add(name: 'no-normal')

		commit()
		grgit.tag.add(name: '0.1.0')

		commit()
		grgit.branch.add(name: 'RB_0.1')

		commit()
		commit()
		grgit.tag.add(name: '0.2.0')
		grgit.branch.add(name: 'RB_0.2')

		grgit.checkout(branch: 'RB_0.1')

		commit()
		grgit.tag.add(name: 'v0.1.1+2010.01.01.12.00.00')

		commit()
		commit()
		commit()
		commit()
		grgit.tag.add(name: 'v0.1.2-milestone.1')

		commit()
		commit()
		commit()
		grgit.checkout(branch: 'master')

		commit()
		grgit.tag.add(name: 'v1.0.0')
		grgit.checkout(branch: 'RB_1.0', createBranch: true)

		commit()
		grgit.checkout(branch: 'master')

		commit()
		grgit.tag.add(name: '1.1.0-rc.1+abcde')

		commit()
	}

	@Unroll('when on #head, #stage version of #scope change infers #expected')
	def 'infers correct version'() {
		given:
		grgit.checkout(branch: head)
		def version = new InferredVersion()
		version.useBuildMetadataForStage = { false }
		version.grgit = grgit
		when:
		version.infer(scope, stage)
		then:
		version.toString() == expected
		where:
		head          | scope   | stage       | expected
		'unreachable' | 'patch' | 'dev'       | '0.0.1-dev.2'
		'unreachable' | 'patch' | 'milestone' | '0.0.1-milestone.1'
		'unreachable' | 'patch' | 'rc'        | '0.0.1-rc.1'
		'unreachable' | 'patch' | 'final'     | '0.0.1'
		'unreachable' | 'minor' | 'dev'       | '0.1.0-dev.2'
		'unreachable' | 'major' | 'dev'       | '1.0.0-dev.2'
		'no-normal'   | 'patch' | 'dev'       | '0.0.1-dev.4'
		'no-normal'   | 'patch' | 'milestone' | '0.0.1-milestone.4'
		'no-normal'   | 'patch' | 'rc'        | '0.0.1-rc.1'
		'no-normal'   | 'patch' | 'final'     | '0.0.1'
		'no-normal'   | 'minor' | 'milestone' | '0.1.0-milestone.1'
		'no-normal'   | 'major' | 'milestone' | '1.0.0-milestone.1'
		'RB_0.1'      | 'patch' | 'dev'       | '0.1.2-dev.7'
		'RB_0.1'      | 'patch' | 'milestone' | '0.1.2-milestone.2'
		'RB_0.1'      | 'patch' | 'rc'        | '0.1.2-rc.1'
		'RB_0.1'      | 'patch' | 'final'     | '0.1.2'
		'RB_0.1'      | 'minor' | 'final'     | '0.2.0'
		'RB_0.1'      | 'major' | 'final'     | '1.0.0'
		'RB_1.0'      | 'minor' | 'dev'       | '1.1.0-dev.1'
		'RB_1.0'      | 'minor' | 'milestone' | '1.1.0-milestone.1'
		'RB_1.0'      | 'minor' | 'rc'        | '1.1.0-rc.1'
		'RB_1.0'      | 'minor' | 'final'     | '1.1.0'
		'RB_1.0'      | 'patch' | 'final'     | '1.0.1'
		'RB_1.0'      | 'major' | 'final'     | '2.0.0'
		'master'      | 'minor' | 'dev'       | '1.1.0-dev.2'
		'master'      | 'minor' | 'milestone' | '1.1.0-milestone.1'
		'master'      | 'minor' | 'rc'        | '1.1.0-rc.2'
		'master'      | 'minor' | 'final'     | '1.1.0'
		'master'      | 'patch' | 'rc'        | '1.0.1-rc.1'
		'master'      | 'major' | 'rc'        | '2.0.0-rc.1'
	}

	def 'cannot infer version if no changes since nearest version'() {
		given:
		grgit.checkout(branch: 'RB_0.2')
		def version = new InferredVersion()
		version.grgit = grgit
		when:
		version.infer('patch', 'dev')
		then:
		thrown(IllegalStateException)
	}

	private void commit() {
		byte[] bytes = new byte[128]
		random.nextBytes(bytes)
		new File(grgit.repository.rootDir, '1.txt') << bytes
		grgit.add(patterns: ['1.txt'])
		grgit.commit(message: 'do')
	}
}
