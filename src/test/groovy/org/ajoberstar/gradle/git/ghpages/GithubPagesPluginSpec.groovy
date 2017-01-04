/*
 * Copyright 2012-2017 the original author or authors.
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

import org.ajoberstar.grgit.Grgit
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

class GithubPagesPluginSpec extends Specification {
    public static final String PLUGIN_NAME = 'org.ajoberstar.github-pages'
    public static final String EXTENSION_NAME = 'githubPages'
    Project project = ProjectBuilder.builder().withProjectDir(new File('.')).build()

    def 'Creates the [githubPages] extension'() {
        assert !project.plugins.hasPlugin(PLUGIN_NAME)
        assert !project.extensions.findByName(EXTENSION_NAME)

        when:
        project.plugins.apply(PLUGIN_NAME)

        then:
        def extension = project.extensions.findByName(EXTENSION_NAME)
        extension instanceof GithubPagesPluginExtension
    }

    @Unroll
    def 'Implements the [#taskName] task'() {
        assert !project.plugins.hasPlugin(PLUGIN_NAME)
        assert !project.tasks.findByName(taskName)

        when:
        project.plugins.apply(PLUGIN_NAME)

        then:
        project.tasks.findByName(taskName)

        where:
        taskName         || _
        'prepareGhPages' || _
        'publishGhPages' || _
    }

    def '[publishGhPages] task depends on [prepareGhPages] tasks'() {
        project.plugins.apply(PLUGIN_NAME)

        when:
        def task = project.tasks.findByName('publishGhPages')

        then:
        task.taskDependencies.getDependencies(task).find { Task it ->
            it.name == 'prepareGhPages'
        }
    }

    @Unroll
    def 'Task [#taskName] has description [#description]'() {
        project.plugins.apply(PLUGIN_NAME)

        when:
        def task = project.tasks.findByName(taskName)

        then:
        task.description == description

        where:
        taskName         || description
        'prepareGhPages' || 'Prepare the gh-pages changes locally'
        'publishGhPages' || 'Publishes all gh-pages changes to Github'
    }

    def '[prepareGhPages] depends on tasks add to the pages spec from'() {
        project.plugins.apply(PLUGIN_NAME)
        project.plugins.apply('java')
        when:
        project.githubPages.pages {
            from project.tasks.javadoc
        }
        def task = project.tasks.findByName('prepareGhPages')
        then:
        task.taskDependencies.getDependencies(task).find { it.name == 'javadoc' }
    }

    // TODO need a test for gh-pages branch creatino if didn't exist before
}
