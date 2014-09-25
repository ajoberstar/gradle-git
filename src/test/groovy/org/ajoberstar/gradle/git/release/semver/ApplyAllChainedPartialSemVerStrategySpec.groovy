package org.ajoberstar.gradle.git.release.semver

import spock.lang.Specification

class ApplyAllChainedPartialSemVerStrategySpec extends Specification {
	def 'infer correctly applies all strategies in order'() {
		given:
		def initialState = new SemVerStrategyState([:])
		def chain = StrategyUtil.all(strategyAppend('a'), strategyAppend('b'), strategyAppend('c'))
		expect:
		chain.infer(initialState) == new SemVerStrategyState(inferredPreRelease: 'a.b.c')
	}

	private def strategyAppend(String str) {
		return StrategyUtil.closure { state ->
			def preRelease = state.inferredPreRelease ? "${state.inferredPreRelease}.${str}" : str
			return state.copyWith(inferredPreRelease: preRelease)
		}
	}
}
