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
import org.ajoberstar.grgit.operation.AddOp
import org.ajoberstar.grgit.util.ConfigureUtil

class StageService {
	private final Repository repository

	StageService(Repository repository) {
		this.repository = repository
	}

	void add(Map parms = [:]) {
		AddOp add = new AddOp(repository)
		ConfigureUtil.configure(add, parms)
		add.call()
	}

	void add(Closure config) {
		AddOp add = new AddOp(repository)
		ConfigureUtil.configure(add, config)
		add.call()
	}

	void remove(Map parms) {

	}

	void reset(Map parms) {

	}

	void apply(Map parms) {

	}
}
