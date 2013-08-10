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

import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Repository
import org.ajoberstar.grgit.exception.GrGitException
import org.ajoberstar.grgit.util.JGitUtil
import org.eclipse.jgit.api.LogCommand
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.lib.ObjectId

/**
 * a
 * @since 0.7.0
 * @author Andrew Oberstar
 */
class LogOp implements Callable<List<Commit>> {
	private Repository repo

	List includes = []
	List excludes = []
	List paths = []
	int skipCommits = 0
	int maxCommits = -1

	LogOp(Repository repo) {
		this.repo = repo
	}

	void range(Object since, Object until) {
		excludes << since
		includes << until
	}

	List<Commit> call() {
		LogCommand cmd = repo.git.log()
		includes.each { include ->
			ObjectId object = JGitUtil.resolveObject(repo, include)
			cmd.add(object)
		}
		excludes.each { exclude ->
			ObjectId object = JGitUtil.resolveObject(repo, exclude)
			cmd.not(object)
		}
		paths.each { path ->
			cmd.addPath(path)
		}
		cmd.skip = skipCommits
		cmd.maxCount = maxCommits
		try {
			return cmd.call().collect { JGitUtil.convertCommit(it) }.asImmutable()
		} catch (GitAPIException e) {
			throw new GrGitException('Problem retrieving log.', e)
		}
	}
}
