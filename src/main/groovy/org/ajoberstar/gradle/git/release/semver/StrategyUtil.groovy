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
package org.ajoberstar.gradle.git.release.semver

/**
 * Utility class to more easily create {@link PartialSemVerStrategy} instances.
 */
final class StrategyUtil {
	private StrategyUtil() {
		throw new AssertionError('Cannot instantiate this class.')
	}

	/**
	 * Creates a strategy backed by the given closure. It should accept and return
	 * a {@link SemVerStrategyState}.
	 */
	static final PartialSemVerStrategy closure(Closure<SemVerStrategyState> behavior) {
		return new ClosureBackedPartialSemVerStrategy(behavior)
	}

	/**
	 * Creates a strategy that applies all of the given strategies in order.
	 */
	static final PartialSemVerStrategy all(PartialSemVerStrategy... strategies) {
		return new ApplyAllChainedPartialSemVerStrategy(strategies as List)
	}

	/**
	 * Creates a strategy that applies each strategy in order, until one changes
	 * the state, which is then returned.
	 */
	static final PartialSemVerStrategy one(PartialSemVerStrategy... strategies) {
		return new ChooseOneChainedPartialSemVerStrategy(strategies as List)
	}

	/**
	 * Returns the int value of a string or returns 0 if it cannot be parsed.
	 */
	static final int parseIntOrZero(String str) {
		try {
			return Integer.parseInt(str)
		} catch (NumberFormatException e) {
			return 0
		}
	}

	/**
	 * Increments the nearest normal version using the specified scope.
	 */
	static final SemVerStrategyState incrementNormalFromScope(SemVerStrategyState state, ChangeScope scope) {
		def oldNormal = state.nearestVersion.normal
		switch (scope) {
			case ChangeScope.MAJOR:
				return state.copyWith(inferredNormal: oldNormal.incrementMajorVersion())
			case ChangeScope.MINOR:
				return state.copyWith(inferredNormal: oldNormal.incrementMinorVersion())
			case ChangeScope.PATCH:
				return state.copyWith(inferredNormal: oldNormal.incrementPatchVersion())
			default:
				return state
		}
	}

	private static class ClosureBackedPartialSemVerStrategy implements PartialSemVerStrategy {
		private final Closure<SemVerStrategyState> behavior

		ClosureBackedPartialSemVerStrategy(Closure<SemVerStrategyState> behavior) {
			this.behavior = behavior
		}

		@Override
		SemVerStrategyState infer(SemVerStrategyState state) {
			return behavior(state)
		}
	}

	private static class ApplyAllChainedPartialSemVerStrategy implements PartialSemVerStrategy {
		private final List<PartialSemVerStrategy> strategies

		ApplyAllChainedPartialSemVerStrategy(List<PartialSemVerStrategy> strategies) {
			this.strategies = strategies
		}

		@Override
		SemVerStrategyState infer(SemVerStrategyState initialState) {
			return strategies.inject(initialState) { state, strategy ->
				strategy.infer(state)
			}
		}
	}

	private static class ChooseOneChainedPartialSemVerStrategy implements PartialSemVerStrategy {
		private final List<PartialSemVerStrategy> strategies

		ChooseOneChainedPartialSemVerStrategy(List<PartialSemVerStrategy> strategies) {
			this.strategies = strategies
		}

		@Override
		SemVerStrategyState infer(SemVerStrategyState oldState) {
			def result = strategies.findResult { strategy ->
				def newState = strategy.infer(oldState)
				oldState == newState ? null : newState
			}
			return result ?: oldState
		}
	}
}
