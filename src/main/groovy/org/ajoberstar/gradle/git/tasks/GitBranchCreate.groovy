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

import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.errors.GitAPIException
import org.gradle.api.GradleException
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction

public class GitBranchCreate extends GitBase {
    String startPoint = 'master'

    @Optional
    String branchName

    @Optional
    CreateBranchCommand.SetupUpstreamMode upstreamMode = CreateBranchCommand.SetupUpstreamMode.TRACK

    @Optional
    boolean force = false

    @TaskAction
    void branchCreate() {
        CreateBranchCommand cmd = getGit().branchCreate()
        cmd.startPoint = startPoint
        cmd.name = branchName
        cmd.upstreamMode = upstreamMode
        cmd.force = force
        try {
            cmd.call()
        } catch (GitAPIException e) {
            throw new GradleException("Problem creating branch", e)
        }
    }
}
