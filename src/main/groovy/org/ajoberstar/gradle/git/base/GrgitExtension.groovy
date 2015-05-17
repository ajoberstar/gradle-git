package org.ajoberstar.gradle.git.base

import org.ajoberstar.grgit.Grgit

/**
 * Extension giving access to a Grgit instance for the
 * repository.
 * @since 1.2.0
 */
class GrgitExtension {
	/**
	 * The repo directory that should be opened by getRepo().
	 */
	File dir

	/**
	 * Opens the repository indicated by dir.
	 */
	Grgit getRepo() {
		return Grgit.open(dir: dir)
	}
}
