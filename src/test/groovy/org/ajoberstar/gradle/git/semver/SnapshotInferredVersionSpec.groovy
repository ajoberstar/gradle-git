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

import spock.lang.Unroll

class SnapshotInferredVersionSpec extends GrgitSpec {
	def setupSpec() {
		commit()
		grgit.branch.add(name: 'mybranch')
		grgit.checkout(branch: 'mybranch')
		grgit.tag.add(name: '3.3.2')
		commit()
	}

	@Unroll('when on #head, #stage version of #scope change infers #expected')
	def 'infers correct version'() {
		given:
		grgit.checkout(branch: head)
		def version = new InferredVersion()
		version.useBuildMetadataForStage = { false }
		version.snapshotStages << 'dev'
		version.grgit = grgit

		when:
		version.infer(scope, stage)

		then:
		version.toString() == expected
		version.releasable

		where:
		head          | scope   | stage       | expected
		'mybranch'    | 'minor' | 'dev'       | '3.4.0-SNAPSHOT'
		'mybranch'    | 'minor' | 'milestone' | '3.4.0-milestone.1'
		'mybranch'    | 'minor' | 'rc'        | '3.4.0-rc.1'
		'mybranch'    | 'minor' | 'final'     | '3.4.0'
		'mybranch'    | 'patch' | 'rc'        | '3.3.3-rc.1'
		'mybranch'    | 'major' | 'final'     | '4.0.0'
	}
}
