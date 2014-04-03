# gradle-git

A set of plugins to support Git in the Gradle build tool.

[![Build Status](https://travis-ci.org/ajoberstar/gradle-git.png?branch=master)](https://travis-ci.org/ajoberstar/gradle-git)

There are two primary use cases for these plugins:
* Publishing to a Github Pages website.  The `github-pages` plugin adds support
for publishing static files to the `gh-pages` branch of a Git repository.
* General Git actions.  The `gradle-git` JAR provides numerous tasks
for basic Git functions that can be performed as part of your build.

For more information see the documentation in the next sections as well as
the full list of tasks and plugins.

**API Documentation:**

* [Javadoc](http://ajoberstar.org/gradle-git/docs/javadoc)
* [Groovydoc](http://ajoberstar.org/gradle-git/docs/groovydoc)

Credit goes to [Peter Ledbrook](https://github.com/pledbrook) for the initial idea.

## Adding the Plugins

Add the following lines to your build to use the `gradle-git` plugins.

    buildscript {
      repositories {
        // jcenter()
        mavenCentral()
      }
      dependencies {
        classpath 'org.ajoberstar:gradle-git:<version>'
      }
    }

## Using Grgit

If all you want to do is use a few of the tasks, there aren't any plugins
to apply.  You merely need to start using the classes:

```groovy
import org.ajoberstar.grgit.*

ext.repo = Grgit.open(project.file('.'))

version = "1.0.0-${repo.head().abbreviatedId}"

task tagRelease << {
  repo.tag.add {
    name = version
    message = "Release of ${version}"
  }
}
```

For details on the available methods/properties see the API docs of
[Grgit](http://ajoberstar.org/grgit/docs/groovydoc/org/ajoberstar/grgit/Grgit.html).
Examples of how to use each operation are provided.

## Using Github Pages

```groovy
apply plugin: 'github-pages'

githubPages {
  repoUri = '...'
  pages {
    from javadoc.outputs.files
  }
}
```

See the [GithubPagesPluginExtension](http://ajoberstar.org/gradle-git/docs/groovydoc/org/ajoberstar/gradle/git/plugins/GithubPagesPluginExtension.html) docs for information on the defaults.

The plugin adds a single `publishGhPages` task that will clone the
repository, copy in the files in the `pages` `CopySpec`, add all of
the changes, commit, and push back to the remote.

## Authentication

Grgit provides multiple ways to authenticate with remote repositories. If using hardcoded credentials, it is suggested to use the system properties rather than including them in the repository.

See Grgit's [AuthConfig](http://ajoberstar.org/grgit/docs/groovydoc/org/ajoberstar/grgit/auth/AuthConfig.html) docs for information on the
various authentication options.

#### Travis CI Notes

In case you plan to use Travis-CI make sure to follow the [travis guide on encrypted keys](http://docs.travis-ci.com/user/encryption-keys/)
to setup an encrypted authentication token like the following:

1. Create a new "Personal Access Token” on Github at https://github.com/settings/applications and name it whatever fits your needs
2. Install the travis CLI on your local machine via “gem install travis” (NOTE: ruby needs to be installed locally!!)
3. Encrypt your Personal Access Token via “travis encrypt GH_TOKEN=<MYTOKENCHARS>”
4. add the encrypted token to your `.travis.yml` file like the following

```ruby
# ...
env:
  global:
  - secure: "E6iGay3wQcbhAUM5S5WkjYUmg6b7oJG9l8T2y0WWRgx50oqR0/jGzCYHpJGCHlb9OOZpB2BnhpYS6fCg09MsPYKcgsMXgjYzozWGBYifBIVNI07zQhDByztWr3fsrwrZc31ifqC3EGL/UEwvN5F093rRufDw2jomGpFQn7gL4Kc="
```

5. Adjust your credentials in the `build.gradle` to something like
```groovy
githubPages {
  // ...
  credentials {
    username = System.getenv('GH_TOKEN')
    password = ''
  }
}
```

## Release Notes

**v0.7.1**
* Fixed `publishGhPages` to actually work. (Sorry about that...)

**v0.7.0**
* Complete rewrite of the Git layer in the [grgit](https://github.com/ajoberstar/grgit) library.
* Git functionality can be used in a more free form manner, rather than dedicated tasks for each operation.
* Breaking Changes:
  * The API has almost completely changed. See the Groovydoc of [gradle-git](http://ajoberstar.org/gradle-git/docs/groovydoc/) and [grgit](http://ajoberstar.org/grgit/docs/groovydoc/index.html) for details.
  * Plugin requires Java 7. Comment on [issue 42](https://github.com/ajoberstar/gradle-git/issues/42) if you have any feedback on that.

**v0.6.5**
* Fixing `GitPush.namesOrSpecs(String...)` to avoid NPE.

**v0.6.4**
* Adding `targetPath` property to `github-pages` plugin to allow pushing to branches besides `gh-pages`. Contributed by [Alexander Heusingfeld](https://github.com/aheusingfeld)
* Fix to bypass SSHAgentConnector in certain situations where correct libraries aren't in place.

**v0.6.3**
* Fixed jsch-agent-proxy support to fall back to other options when agents aren't really available. See #31.

**v0.6.2**
* Added `GitInit` to simply initialize a new local Git repo. Contributed by [Rasmus Praestholm](https://github.com/Cervator)

**v0.6.1**
* Updated `GitPush` to support specifying branch names or specs to push. Contributed by [Benjamin Muschko](https://github.com/bmuschko)

**v0.6.0**
* Added support for jsch-agent-proxy. This allows use of sshagent and Pageant to provide ssh credentials.

**v0.5.0**

* Added support for checking out tags on `GitClone`.
* Fixed a bug with `GitAdd` that prevented  adding individual files from a subdirectory of the repository.

**v0.4.0**

* Minor update to `GitCommit` to allow include/exclude rules for the files to commit, courtesy of [Evgeny Shepelyuk](https://github.com/eshepelyuk).

**v0.3.0**

* Added `GitBranchCreate`, `GitBranchList`, `GitBranchTrackingStatus`, `GitCheckout`, `GitStatus`
tasks courtesy of [Evgeny Shepelyuk](https://github.com/eshepelyuk).

**v0.2.3**

* Added `GitClean` and `GitLog` tasks contributed by [Alex Lixandru](https://github.com/alixandru)

NOTE: The `GitLog` tag only supports commit hashes (abbreviated or full).  Tag names do not work
right now.

**v0.2.2**

* Fix: If `cloneGhPages` does not checkout the `gh-pages` branch, most likely because it doesn't
exist, the task will fail.

**v0.2.1**

* Added `GitFetch`, `GitMerge`, `GitPull`, and `GitReset` tasks contributed
by [Alex Lixandru](https://github.com/alixandru).

**v0.2.0**

This release does contain breaking changes.

* Consolidated plugins into `GithubPagesPlugin`.  The existing `GithubPlugin`
and `GitPlugin` provided no useful functionality.
* Centralized implementation for retrieving authentication.

**v0.1.2**

* Added support for SSH connections with the help of [Urs](https://github.com/UrsKR)

**v0.1.1**

* Added `GitTag` task contributed by [Urs Reupke](https://github.com/UrsKR).
* The `repoPath` for all Git tasks is defaulted to the root project's directory.

**v0.1.0**

Initial release.

