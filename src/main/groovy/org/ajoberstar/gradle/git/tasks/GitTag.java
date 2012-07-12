package org.ajoberstar.gradle.git.tasks;

import groovy.lang.Closure;
import org.gradle.api.tasks.TaskAction;
import org.eclipse.jgit.api.CommitCommand;
import org.gradle.api.GradleException;
import org.eclipse.jgit.api.TagCommand;
import org.eclipse.jgit.lib.PersonIdent;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.util.ConfigureUtil;
import org.ajoberstar.gradle.util.ObjectUtil;

public class GitTag extends GitBase {
  private PersonIdent tagger = null;
  private Object message = null;
  private Object tagName = null;

  /**
   * Tags the HEAD.
   */
  @TaskAction
  public void tag() {
    TagCommand cmd = getGit().tag();
    if (message != null) {
      cmd.setMessage(getMessage());
    }
    if (tagger != null) {
      cmd.setTagger(getTagger());
    }
    cmd.setName(getTagName());
    try {
      cmd.call();
    } catch (Exception e) {
      throw new GradleException("Problem tagging revision.", e);
    }
  }

  @Input
  @Optional
  public PersonIdent getTagger() {
    return tagger;
  }

  /**
   * Sets the tagger.
   * @param tagger the tagger
   */
  public void setTagger(PersonIdent tagger) {
    this.tagger = tagger;
  }

  /**
   * Configures the tagger.
   * A {@code PersonIdent} is passed to the closure.
   * @param config the configuration closure
   */
  @SuppressWarnings("rawtypes")
  public void tagger(Closure config) {
    if (tagger == null) {
      this.tagger = new PersonIdent(getGit().getRepository());
    }
    ConfigureUtil.configure(config, tagger);
  }

  /**
   * Gets the tag message to use.
   * @return the tag message to use
   */
  @Input
  @Optional
  public String getMessage() {
    return ObjectUtil.unpackString(message);
  }

  /**
   * Sets the tag message to use.
   * @param message the tag message
   */
  public void setMessage(Object message) {
    this.message = message;
  }

  /**
   * Gets the tag name to use.
   * @return the tag name to use
   */
  @Input
  public String getTagName() {
    return ObjectUtil.unpackString(tagName);
  }

  /**
   * Sets the tag name to use.
   * @param tagName the tag name
   */
  public void setTagName(Object tagName) {
    this.tagName = tagName;
  }

}
