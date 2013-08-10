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
import org.ajoberstar.grgit.Status
import org.ajoberstar.grgit.exception.GrGitException
import org.ajoberstar.grgit.util.JGitUtil

import org.eclipse.jgit.api.StatusCommand
import org.eclipse.jgit.api.errors.GitAPIException

class StatusOp implements Callable<Status> {
	private final Repository repo

	StatusOp(Repository repo) {
		this.repo = repo
	}

	Status call() {
		StatusCommand cmd = repo.git.status()
		try {
			return JGitUtil.convertStatus(cmd.call())
		} catch (GitAPIException e) {
			throw new GrGitException('Problem retrieving status.', e)
		}
	}
}
