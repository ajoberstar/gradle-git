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
package org.ajoberstar.gradle.git.ghpages

import org.ajoberstar.gradle.git.auth.BasicPasswordCredentials
import org.gradle.api.Project
import org.gradle.api.file.CopySpec
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

class GithubPagesPluginExtensionSpec extends Specification {
	Project project = ProjectBuilder.builder().build()

	def 'Verify the defaults'() {
		def ext = new GithubPagesPluginExtension(project)

		expect:
		ext.commitMessage == 'Publish of Github pages from Gradle.'
		ext.credentials instanceof BasicPasswordCredentials
		ext.pages instanceof CopySpec
		ext.targetBranch == 'gh-pages'
		ext.workingDir == project.file("${project.buildDir}/ghpages")
		ext.workingPath == "${project.buildDir}/ghpages"
		ext.deleteExistingFiles
		!ext.repoUri
	}

	@Unroll
	def 'Repo uri [#repoUri] results in [#expected]'() {
		def ext = new GithubPagesPluginExtension(project)
		ext.repoUri = repoUri

		expect:
		ext.getRepoUri() == expected

		where:
		repoUri                        || expected
		'git@domain.tld:user/repo.git' || 'git@domain.tld:user/repo.git'
		// as repoUri is an Object which gets "unpacked", I expect there are
		// use-cases for this. But I've none
	}

	def 'WorkingDir is based on workingPath'() {
		def ext = new GithubPagesPluginExtension(project)
		ext.workingPath = "${project.buildDir}${File.separator}some-path"

		when:
		def workingDir = ext.getWorkingDir()

		then:
		workingDir.absolutePath.endsWith(old(ext.workingPath))
	}
}
