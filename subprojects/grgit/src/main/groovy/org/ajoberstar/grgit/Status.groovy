/*
 * Copyright 2012-2013 the original author or authors.
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
package org.ajoberstar.grgit

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@EqualsAndHashCode
@ToString(includeNames=true)
class Status {
	final Changes staged
	final Changes unstaged

	Status(
		Set<? extends String> stagedNewFiles,
		Set<? extends String> stagedModifiedFiles,
		Set<? extends String> stagedDeletedFiles,
		Set<? extends String> unstagedNewFiles,
		Set<? extends String> unstagedModifiedFiles,
		Set<? extends String> unstagedDeletedFiles
	) {
		this.staged = new Changes(stagedNewFiles, stagedModifiedFiles, stagedDeletedFiles)
		this.unstaged = new Changes(unstagedNewFiles, unstagedModifiedFiles, unstagedDeletedFiles)
	}

	@EqualsAndHashCode
	@ToString(includeNames=true)
	class Changes {
		final Set<String> newFiles
		final Set<String> modifiedFiles
		final Set<String> deletedFiles

		private Changes(
			Set<? extends String> newFiles,
			Set<? extends String> modifiedFiles,
			Set<? extends String> deletedFiles
		) {
			this.newFiles = newFiles
			this.modifiedFiles = modifiedFiles
			this.deletedFiles = deletedFiles
		}

		Set<String> getAllChanges() {
			return newFiles + modifiedFiles + deletedFiles
		}
	}

	boolean isClean() {
		return (staged.allChanges + unstaged.allChanges).empty
	}
}
