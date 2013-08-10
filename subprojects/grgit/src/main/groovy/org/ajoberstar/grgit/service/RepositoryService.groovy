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
package org.ajoberstar.grgit.service

import org.ajoberstar.grgit.Repository
import org.ajoberstar.grgit.Status
import org.ajoberstar.grgit.operation.StatusOp
import org.ajoberstar.grgit.util.ConfigureUtil

/**
 *
 * @since 0.7.0
 * @author Andrew Oberstar
 */
class RepositoryService {
	final Repository repository

	final HistoryService history
	final StageService stage

	final BranchService branches
	final NoteService notes
	final RemoteService remotes
	final StashService stashes
	final TagService tags

	final Status status

	RepositoryService(Repository repository) {
		this.repository = repository
		this.history = new HistoryService(repository)
		this.stage = new StageService(repository)
		this.branches = null
		this.notes = null
		this.remotes = null
		this.stashes = null
		this.tags = null
		this.status = null
	}

	Status status(Map parms = [:]) {
		StatusOp status = new StatusOp(repository)
		ConfigureUtil.configure(status, parms)
		return status.call()
	}

	Status status(Closure config) {
		StatusOp status = new StatusOp(repository)
		ConfigureUtil.configure(status, config)
		return status.call()
	}

	boolean isBare() {

	}

	void clean(Map parms) {

	}

	void garbageCollect(Map parms) {

	}
}
