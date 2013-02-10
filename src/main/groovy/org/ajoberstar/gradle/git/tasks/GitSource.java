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
import org.gradle.api.file.FileTree;
import org.gradle.api.file.FileTreeElement;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.util.PatternFilterable;
import org.gradle.api.tasks.util.PatternSet;

import java.util.Set;

/**
 * Base class for Git commands that act upon
 * source files within the repository.
 *
 * @since 0.1.0
 */
public abstract class GitSource extends GitBase implements PatternFilterable {
    protected PatternFilterable patternSet = new PatternSet();

    /**
     * Gets the source files this task will act on.
     * The patterns configured on this task are evaluated
     * against the repo directory to determine the files.
     *
     * @return the source files
     */
    @InputFiles
    public FileTree getSource() {
        FileTree src = getProject().fileTree(getRepoDir(), null);
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
    @SuppressWarnings("rawtypes")
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
    @SuppressWarnings("rawtypes")
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
