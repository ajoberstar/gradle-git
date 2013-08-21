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
package org.ajoberstar.grgit.operation

import java.util.concurrent.Callable

import org.ajoberstar.grgit.Repository
import org.ajoberstar.grgit.exception.GrGitException

import org.eclipse.jgit.api.ResetCommand
import org.eclipse.jgit.api.errors.GitAPIException

class ResetOp implements Callable<Void> {
	private Repository repo

	Set<String> paths = []
	String commit
	Mode mode = Mode.MIXED

	ResetOp(Repository repo) {
		this.repo = repo
	}

	Void call() {
		ResetCommand cmd = repo.git.reset()
		paths.each { cmd.addPath(it) }
		cmd.ref = commit
		cmd.mode = mode.toJGit()
		try {
			cmd.call()
			return null
		} catch (GitAPIException e) {
			throw new GrGitException('Problem running reset.', e)
		}
	}

	enum Mode {
		HARD(ResetCommand.ResetType.HARD),
		KEEP(ResetCommand.ResetType.KEEP),
		MERGE(ResetCommand.ResetType.MERGE),
		MIXED(ResetCommand.ResetType.MIXED),
		SOFT(ResetCommand.ResetType.SOFT)

		private final ResetCommand.ResetType jgit

		private Mode(ResetCommand.ResetType jgit) {
			this.jgit = jgit
		}

		private ResetCommand.ResetType toJGit() {
			return jgit
		}
	}
}
