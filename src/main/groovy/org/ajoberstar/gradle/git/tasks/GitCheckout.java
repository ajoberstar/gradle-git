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

import org.eclipse.jgit.api.CheckoutCommand;
import org.eclipse.jgit.lib.Constants;
import org.gradle.api.GradleException;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.tasks.TaskAction;

/**
 * Task to checkout files and refs
 *
 * @since 0.3.0
 */
@SuppressWarnings("JavaDoc")
public class GitCheckout extends GitSource {

  public String startPoint;

  public String name = Constants.MASTER;

  public boolean createBranch = false;

  @TaskAction
  void checkout() {
    final CheckoutCommand cmd = getGit().checkout();
    cmd.setStartPoint(startPoint);
    cmd.setName(name);
    cmd.setCreateBranch(createBranch);

    if (!patternSet.getExcludes().isEmpty() || !patternSet.getIncludes().isEmpty()) {
      getSource().visit(new FileVisitor() {
        public void visitDir(FileVisitDetails arg0) {
          visitFile(arg0);
        }

        public void visitFile(FileVisitDetails arg0) {
          cmd.addPath(arg0.getPath());
        }
      });
    }

    try {
      cmd.call();
    } catch (Exception e) {
      throw new GradleException("Problem checking out from repository", e);
    }
  }

  /**
   * Set starting point for Git checkout
   *
   * @param startPoint
   */
  public void setStartPoint(String startPoint) {
    this.startPoint = startPoint;
  }

  /**
   * Specify the name of the branch or commit to check out, or the new branch name.
   *
   * @param name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Specify whether to create a new branch.
   *
   * @param createBranch
   */
  public void setCreateBranch(boolean createBranch) {
    this.createBranch = createBranch;
  }
}
