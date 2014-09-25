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
package org.ajoberstar.gradle.git.release.semver

import spock.lang.Specification

class ChooseOneChainedPartialSemVerStrategySpec extends Specification {
	def 'infer correctly picks the first strategy that changes state'() {
		given:
		def initialState = new SemVerStrategyState([:])
		def chain = StrategyUtil.one(nothing(), string('a'), string('b'), nothing())
		expect:
		chain.infer(initialState) == new SemVerStrategyState(inferredPreRelease: 'a')
	}

	private def nothing() {
		return StrategyUtil.closure { state -> state }
	}

	private def string(String str) {
		return StrategyUtil.closure { state -> state.copyWith(inferredPreRelease: str) }
	}
}
