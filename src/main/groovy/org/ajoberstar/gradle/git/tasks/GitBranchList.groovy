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
package org.ajoberstar.gradle.git.tasks

import org.eclipse.jgit.api.ListBranchCommand
import org.eclipse.jgit.api.errors.GitAPIException
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction

public class GitBranchList extends GitBase {

    boolean showAll

    @TaskAction
    void branchList() {
        ListBranchCommand cmd = getGit().branchList()
        if (showAll) cmd.listMode = ListBranchCommand.ListMode.ALL
        try {
            extensions.extraProperties.set "branches", cmd.call().collect { it.name }
        } catch (GitAPIException e) {
            throw new GradleException("Problem listing branches", e)
        }
    }
}
