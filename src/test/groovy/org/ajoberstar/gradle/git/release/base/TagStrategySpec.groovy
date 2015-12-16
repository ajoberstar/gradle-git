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
package org.ajoberstar.gradle.git.release.base

import org.ajoberstar.grgit.Grgit
import org.ajoberstar.grgit.service.TagService
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class TagStrategySpec extends Specification {
	def 'maybeCreateTag with version create tag true will create a tag'() {
		given:
		Grgit grgit = GroovyMock()
		TagService tag = GroovyMock()
		Project project = ProjectBuilder.builder().withName("testproj").build()
		grgit.tag >> tag
		1 * tag.add([name: 'v1.2.3', message: 'Release of 1.2.3'])
		0 * tag._
		expect:
		new TagStrategy(new ReleasePluginExtension(project)).maybeCreateTag(grgit, new ReleaseVersion('1.2.3', null, true)) == 'v1.2.3'
	}

	def 'maybeCreateTag with version create tag false does not create a tag'() {
		given:
		Grgit grgit = GroovyMock()
		TagService tag = GroovyMock()
		Project project = ProjectBuilder.builder().withName("testproj").build()
		grgit.tag >> tag
		0 * tag._
		expect:
		new TagStrategy(new ReleasePluginExtension(project)).maybeCreateTag(grgit, new ReleaseVersion('1.2.3', null, false)) == null
	}

	def 'maybeCreateTag with version create tag true and prefix name with v false will create a tag'() {
		given:
		Grgit grgit = GroovyMock()
		TagService tag = GroovyMock()
		Project project = ProjectBuilder.builder().withName("testproj").build()
		grgit.tag >> tag
		1 * tag.add([name: '1.2.3', message: 'Release of 1.2.3'])
		0 * tag._
		def strategy = new TagStrategy(new ReleasePluginExtension(project))
		strategy.prefixNameWithV = false
		expect:
		strategy.maybeCreateTag(grgit, new ReleaseVersion('1.2.3', null, true)) == '1.2.3'
	}
}
