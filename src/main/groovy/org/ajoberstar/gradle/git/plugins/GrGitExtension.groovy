package org.ajoberstar.gradle.git.plugins

import org.ajoberstar.grgit.Repository
import org.ajoberstar.grgit.service.RepositoryService
import org.ajoberstar.grgit.service.ServiceFactory
import org.gradle.api.Project

class GrGitExtension {
	private final Project project

	GrGitExtension(Project project) {
		this.project = project
	}

	RepositoryService repo(Object path) {
		Repository repo = ServiceFactory.createRepository(project.file(path))
		return ServiceFactory.createService(repo)
	}
}
