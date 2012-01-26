/* Copyright 2012 the original author or authors.
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
package org.ajoberstar.gradle.git.tasks;

import org.eclipse.jgit.api.AddCommand;
import org.eclipse.jgit.api.errors.NoFilepatternException;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.TaskAction;

/**
 * 
 * 
 * @since 0.1.0
 */
public class GitAdd extends GitSource {
	private boolean ignoreUntracked = false;
	
	@TaskAction
	void add() {
		final AddCommand cmd = getGit().add();
		cmd.setUpdate(isIgnoreUntracked());
		
		getSource().visit(new FileVisitor() {
			public void visitDir(FileVisitDetails arg0) {
				visitFile(arg0);
			}

			public void visitFile(FileVisitDetails arg0) {
				cmd.addFilepattern(arg0.getPath());
			}
		});
		
		try {
			cmd.call();
		} catch (NoFilepatternException e) {
			getLogger().info("No files included in the add command for task {}", getName());
		}
	}
	
	@Input
	public boolean isIgnoreUntracked() {
		return ignoreUntracked;
	}
	
	public void setIgnoreUntracked(boolean ignoreUntracked) {
		this.ignoreUntracked = ignoreUntracked;
	}
}
