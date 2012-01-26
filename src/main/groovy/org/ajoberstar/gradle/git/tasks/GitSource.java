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

import groovy.lang.Closure;

import java.util.Set;

import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;

/**
 * 
 * @since 0.1.0
 */
public abstract class GitSource extends GitBase implements PatternFilterable {
	private PatternFilterable patternSet = new PatternSet();
	
	@InputFiles
	public FileTree getSource() {
		FileTree src = getProject().fileTree(getRepoDir());
		return src.matching(patternSet);
	}
	
	/**
	 * {@inheritDoc}
	 */
	public PatternFilterable exclude(String... arg0) {
		return patternSet.exclude(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	public PatternFilterable exclude(Iterable<String> arg0) {
		return patternSet.exclude(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	public PatternFilterable exclude(Spec<FileTreeElement> arg0) {
		return patternSet.exclude(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	public PatternFilterable exclude(Closure arg0) {
		return patternSet.exclude(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getExcludes() {
		return patternSet.getExcludes();
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<String> getIncludes() {
		return patternSet.getIncludes();
	}

	/**
	 * {@inheritDoc}
	 */
	public PatternFilterable include(String... arg0) {
		return patternSet.include(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	public PatternFilterable include(Iterable<String> arg0) {
		return patternSet.include(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	public PatternFilterable include(Spec<FileTreeElement> arg0) {
		return patternSet.include(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	public PatternFilterable include(Closure arg0) {
		return patternSet.include(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	public PatternFilterable setExcludes(Iterable<String> arg0) {
		return patternSet.setExcludes(arg0);
	}

	/**
	 * {@inheritDoc}
	 */
	public PatternFilterable setIncludes(Iterable<String> arg0) {
		return patternSet.setIncludes(arg0);
	}
}
