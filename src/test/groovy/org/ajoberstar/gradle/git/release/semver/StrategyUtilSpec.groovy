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

import spock.lang.Specification

class StrategyUtilSpec extends Specification {
    SemVerStrategyState initialState = new SemVerStrategyState([:])

    def 'closure backed uses behavior passed in'() {
        expect:
        stringReplace('a').infer(initialState) == new SemVerStrategyState(inferredPreRelease: 'a')
    }

    def 'choose one returns the first that changes state'() {
        given:
        def chain = StrategyUtil.one(nothing(), stringReplace('a'), stringReplace('b'), nothing())
        expect:
        chain.infer(initialState) == new SemVerStrategyState(inferredPreRelease: 'a')
    }

    def 'apply all uses all strategies in order'() {
        given:
        def chain = StrategyUtil.all(stringAppend('a'), stringAppend('b'), stringAppend('c'))
        expect:
        chain.infer(initialState) == new SemVerStrategyState(inferredPreRelease: 'a.b.c')
    }

    private def nothing() {
        return StrategyUtil.closure { state -> state }
    }

    private def stringReplace(String str) {
        return StrategyUtil.closure { state -> state.copyWith(inferredPreRelease: str) }
    }

    private def stringAppend(String str) {
        return StrategyUtil.closure { state ->
            def preRelease = state.inferredPreRelease ? "${state.inferredPreRelease}.${str}" : str
            return state.copyWith(inferredPreRelease: preRelease)
        }
    }
}
