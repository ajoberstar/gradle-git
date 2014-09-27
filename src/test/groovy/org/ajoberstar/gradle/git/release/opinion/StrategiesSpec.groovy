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
package org.ajoberstar.gradle.git.release.opinion

import spock.lang.Specification
import spock.lang.Unroll
import org.ajoberstar.grgit.Branch
import org.ajoberstar.gradle.git.release.semver.SemVerStrategyState
import org.ajoberstar.gradle.git.release.semver.ChangeScope
import org.ajoberstar.gradle.git.release.semver.NearestVersion
import com.github.zafarkhaja.semver.Version
import org.gradle.api.GradleException

class StrategiesSpec extends Specification {
	SemVerStrategyState initialState = new SemVerStrategyState([:])
	@Unroll('Normal.USE_SCOPE_PROP with scope #scope results in #version')
	def 'Normal.USE_SCOPE_PROP'() {
		def nearestVersion = new NearestVersion(normal: Version.valueOf('1.2.3'))
		def initialState = new SemVerStrategyState(
			scopeFromProp: scope,
			nearestVersion: nearestVersion)
		expect:
		Strategies.Normal.USE_SCOPE_PROP.infer(initialState) ==
			new SemVerStrategyState(
				scopeFromProp: scope,
				nearestVersion: nearestVersion,
				inferredNormal: version)
		where:
		scope | version
		ChangeScope.PATCH | '1.2.4'
		ChangeScope.MINOR | '1.3.0'
		ChangeScope.MAJOR | '2.0.0'
	}

	def 'Normal.USE_NEAREST_ANY with different nearest any and normal uses any\'s normal'() {
		given:
		def nearestVersion = new NearestVersion(
			any: Version.valueOf('1.2.5-beta.1'),
			normal: Version.valueOf('1.2.4'))
		def initialState = new SemVerStrategyState(nearestVersion: nearestVersion)
		expect:
		Strategies.Normal.USE_NEAREST_ANY.infer(initialState) ==
			new SemVerStrategyState(nearestVersion: nearestVersion, inferredNormal: '1.2.5')
	}


	def 'Normal.USE_NEAREST_ANY with same nearest any and normal does nothing'() {
		given:
		def initialState = new SemVerStrategyState(
			nearestVersion: new NearestVersion(
				any: Version.valueOf('1.2.4'),
				normal: Version.valueOf('1.2.4')))
		expect:
		Strategies.Normal.USE_NEAREST_ANY.infer(initialState) == initialState
	}

	def 'Normal.ENFORCE_BRANCH_MAJOR_X fails if nearest normal can\'t be incremented to something that complies with branch'() {
		given:
		def initialState = new SemVerStrategyState(
			nearestVersion: new NearestVersion(normal: Version.valueOf('1.2.3')),
			currentBranch: new Branch(fullName: 'refs/heads/3.x'))
		when:
		Strategies.Normal.ENFORCE_BRANCH_MAJOR_X.infer(initialState)
		then:
		thrown(GradleException)
		where:
		version << ['1.2.3', '3.1.0', '4.0.0']
	}

	def 'Normal.ENFORCE_BRANCH_MAJOR_X correctly increments version to comply with branch'() {
		given:
		def initialState = new SemVerStrategyState(
			nearestVersion: new NearestVersion(normal: Version.valueOf(nearest)),
			currentBranch: new Branch(fullName: 'refs/heads/3.x'))
		expect:
		Strategies.Normal.ENFORCE_BRANCH_MAJOR_X.infer(initialState) ==
			initialState.copyWith(inferredNormal: inferred)
		where:
		nearest | inferred
		'2.0.0' | '3.0.0'
		'2.3.0' | '3.0.0'
		'2.3.4' | '3.0.0'
		'3.0.0' | null
		'3.1.0' | null
		'3.1.2' | null
	}

	def 'Normal.ENFORCE_BRANCH_MAJOR_MINOR_X fails if nearest normal can\'t be incremented to something that complies with branch'() {
		given:
		def initialState = new SemVerStrategyState(
			nearestVersion: new NearestVersion(normal: Version.valueOf(version)),
			currentBranch: new Branch(fullName: 'refs/heads/3.2.x'))
		when:
		Strategies.Normal.ENFORCE_BRANCH_MAJOR_MINOR_X.infer(initialState)
		then:
		thrown(GradleException)
		where:
		version << ['1.2.3', '3.0.0', '3.3.0', '4.2.0']
	}

	def 'Normal.ENFORCE_BRANCH_MAJOR_MINOR_X correctly increments version to comply with branch'() {
		given:
		def initialState = new SemVerStrategyState(
			nearestVersion: new NearestVersion(normal: Version.valueOf(nearest)),
			currentBranch: new Branch(fullName: "refs/heads/${branch}"))
		expect:
		Strategies.Normal.ENFORCE_BRANCH_MAJOR_MINOR_X.infer(initialState) ==
			initialState.copyWith(inferredNormal: inferred)
		where:
		branch  | nearest | inferred
		'3.0.x' | '2.0.0' | '3.0.0'
		'3.0.x' | '2.1.0' | '3.0.0'
		'3.0.x' | '2.1.2' | '3.0.0'
		'3.0.x' | '3.0.0' | null
		'3.0.x' | '3.0.1' | null
		'3.2.x' | '3.1.0' | '3.2.0'
		'3.2.x' | '3.1.2' | '3.2.0'
		'3.2.x' | '3.2.0' | null
		'3.2.x' | '3.2.1' | null
	}

	def 'Normal.useScope correctly increments normal'() {
		given:
		def initialState = new SemVerStrategyState(
			nearestVersion: new NearestVersion(normal: Version.valueOf('1.2.3')))
		expect:
		Strategies.Normal.useScope(scope).infer(initialState) == initialState.copyWith(inferredNormal: inferred)
		where:
		scope             | inferred
		ChangeScope.PATCH | '1.2.4'
		ChangeScope.MINOR | '1.3.0'
		ChangeScope.MAJOR | '2.0.0'
	}
}
