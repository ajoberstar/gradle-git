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

final class StrategyUtil {
	private StrategyUtil() {
		throw new AssertionError('Cannot instantiate this class.')
	}

	static final PartialSemVerStrategy closure(Closure behavior) {
		return new ClosureBackedPartialSemVerStrategy(behavior)
	}

	static final PartialSemVerStrategy all(PartialSemVerStrategy... strategies) {
		return new ApplyAllChainedPartialSemVerStrategy(strategies as List)
	}

	static final PartialSemVerStrategy one(PartialSemVerStrategy... strategies) {
		return new ChooseOneChainedPartialSemVerStrategy(strategies as List)
	}

	static final int parseIntOrZero(String str) {
		try {
			return Integer.parseInt(str)
		} catch (NumberFormatException e) {
			return 0
		}
	}
}
