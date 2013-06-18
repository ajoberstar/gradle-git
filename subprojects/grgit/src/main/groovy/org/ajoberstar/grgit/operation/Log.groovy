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

import org.ajoberstar.grgit.Repository
import org.ajoberstar.grgit.exception.GrGitException
import org.eclipse.jgit.api.LogCommand
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.lib.ObjectId

/**
 * a
 * @since 0.7.0
 * @author Andrew Oberstar
 */
class Log {
	private Repository repo

	List includes = []
	List excludes = []
	int skipCommits = 0
	int maxCommits = -1

	Log(Repository repo) {
		this.repo = repo	
	}

	List call() {
		LogCommand cmd = repo.git.log()
		includes.each { include ->
			ObjectId object = JGitUtil.resolveObject(include)
			cmd.add(object)
		}
		excludes.each { exclude ->
			ObjectId object = JGitUtil.resolveObject(exclude)
			cmd.not(object)
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
