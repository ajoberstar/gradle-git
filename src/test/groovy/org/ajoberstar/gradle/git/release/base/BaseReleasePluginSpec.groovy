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
package org.ajoberstar.gradle.git.release.base

import spock.lang.Specification

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.Branch
import org.ajoberstar.grgit.service.BranchService
import org.ajoberstar.grgit.service.TagService
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder

class BaseReleasePluginSpec extends Specification {
	Project project = ProjectBuilder.builder().build()

	def setup() {
		project.plugins.apply('org.ajoberstar.release-base')
	}

	def 'release task pushes branch and tag if created'() {
		given:
		Grgit repo = GroovyMock()
		BranchService branch = GroovyMock()
		repo.branch >> branch
		TagService tag = GroovyMock()
		repo.tag >> tag
		branch.current >> new Branch(fullName: 'refs/heads/master')
		project.release {
			versionStrategy([
				getName: { 'a' },
				selector: {proj, repo2 -> true },
				infer: {proj, repo2 -> new ReleaseVersion('1.2.3', true)}] as VersionStrategy)
			grgit = repo
		}
		when:
		project.tasks.release.execute()
		then:
		1 * repo.push([remote: 'origin', refsOrSpecs: ['refs/heads/master', 'v1.2.3']])
	}

	def 'release task pushes branch but not tag if it was not created'() {
		given:
		Grgit repo = GroovyMock()
		BranchService branch = GroovyMock()
		repo.branch >> branch
		TagService tag = GroovyMock()
		repo.tag >> tag
		branch.current >> new Branch(fullName: 'refs/heads/master')
		project.release {
			versionStrategy([
				getName: { 'a' },
				selector: {proj, repo2 -> true },
				infer: {proj, repo2 -> new ReleaseVersion('1.2.3', false)}] as VersionStrategy)
			grgit = repo
		}
		when:
		project.tasks.release.execute()
		then:
		1 * repo.push([remote: 'origin', refsOrSpecs: ['refs/heads/master']])
	}
}
