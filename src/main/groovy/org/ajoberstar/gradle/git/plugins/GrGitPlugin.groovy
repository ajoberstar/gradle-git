package org.ajoberstar.gradle.git.plugins

import org.gradle.api.Plugin
import org.gradle.api.Project

class GrGitPlugin implements Plugin<Project> {
	void apply(Project project) {
		project.extensions.create('grgit', GrGitExtension, project)
	}
}
