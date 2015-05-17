package org.ajoberstar.gradle.git.base

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin providing access to a Grgit instance for your repo.
 * Defaults to using the root projects directory.
 * @since 1.2.0
 */
class GrgitPlugin implements Plugin<Project> {
	@Override
	void apply(Project project) {
		def ext = project.extensions.create('grgit', GrgitExtension)
		ext.dir = project.rootDir
	}
}
