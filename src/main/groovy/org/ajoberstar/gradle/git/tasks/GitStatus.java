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

import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.gradle.api.GradleException;
import org.gradle.api.tasks.TaskAction;

import java.util.Set;

public class GitStatus extends GitBase {

  private Status status;

  @TaskAction
  public void status() {
    StatusCommand cmd = getGit().status();
    try {
      this.status = cmd.call();
    } catch (GitAPIException e) {
      throw new GradleException("Problem obtaining status", e);
    }
  }

  public Set<String> getUntracked() {
    if (status == null) {
      throw new IllegalStateException("Task has not executed yet.");
    }
    return status.getUntracked();
  }

  public Set<String> getModified() {
    if (status == null) {
      throw new IllegalStateException("Task has not executed yet.");
    }
    return status.getModified();
  }

  public Set<String> getChanged() {
    if (status == null) {
      throw new IllegalStateException("Task has not executed yet.");
    }
    return status.getChanged();
  }

  public Set<String> getAdded() {
    if (status == null) {
      throw new IllegalStateException("Task has not executed yet.");
    }
    return status.getAdded();
  }

  public Set<String> getMissing() {
    if (status == null) {
      throw new IllegalStateException("Task has not executed yet.");
    }
    return status.getMissing();
  }
}
