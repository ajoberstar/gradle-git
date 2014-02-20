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

---

## Adding the Plugins

Add the following lines to your build to use the gradle-git plugins.

    buildscript {
      repositories { mavenCentral() }
      dependencies { classpath 'org.ajoberstar:gradle-git:0.6.3' }
    }

## Using Tasks

If all you want to do is use a few of the tasks, there aren't any plugins
to apply.  You merely need to start using the tasks:

```groovy
import org.ajoberstar.gradle.git.tasks.*

task tag(type: GitTag) {
	tagName = version
	message = "Release of ${version}"
}
```

For details on the available methods/properties see the API docs listed above.

## Repository

By default all `GitBase` tasks (any task that acts on an existing local
repository, i.e. everything except `GitClone`) will act on the repository
stored in the root Gradle project's directory.

This can be overriden with the following:

```groovy
task add(type: GitAdd) {
	repoPath = 'some/other/place/to/look'
}
```

## Authentication

All authentication methods supported by JGit should be supported by these
plugins.  However, the only ones that are tested are:
* Username/Password
* SSH (with or without a passphrase)
  * sshagent and Pageant are also supported

On any task that supports/requires credentials, you will have two options for
configuration:

### Programmatic Username/Password

Use the `credentials` property/method to configure username/password creds

```groovy
task push(type: GitPush) {
  credentials {
    username = 'something'
    password = 'somethingSecret'
  }
}
```

It is unlikely that you would hardcode your password into the build file, so
you should store these in another file, such as the user level Gradle properties
(`~/.gradle/gradle.properties`).

### Prompt for Credentials

If no username/password credentials are provided programmatically, you will be
prompted for any necessary credentials at execution time.  This method has been
tested with username/password auth, as well as SSH w/ passphrase auth.

## Github Pages Plugin

To apply the Github Pages plugin add the following line to your build:

    apply plugin: 'github-pages'

This configures tasks needed to clone, add, commit, and push changes to the
gh-pages branch of your Github repository.

### Configuring Repository To Push To

The repository that the pages will be pushed to is configured via the
`githubPages` extension:

```
githubPages {
  repoUri = 'git@github.com:ajoberstar/gradle-git.git'
  targetBranch = master
}
```

### Configuring Files to Publish

The files that will be published to gh-pages are in the `githubPages.pages`
CopySpec. By default all files in `src/main/ghpages` will be included. The
default location the repository will be cloned to is `build/ghpages`. This
can be configured with `githubPages.workingPath`.

```
githubPages {
  pages {
    from(javadoc.outputs.files) {
      into 'docs/javadoc'
    }
    from(groovydoc.outputs.files) {
      into 'docs/groovydoc'
    }
  }
  workingPath = 'build/somewhere/else'
}
```

To publish your changes run:

```
./gradlew publishGhPages
```

### Properties-Based Authentication

Beyond what is mentioned above, the github-pages plugin also provides a
file based way to authenticate.  If you are using username/password
credentials and don't want to re-enter them during each build, you can
specify the credentials in the `gradle.properties` file.  As these are
sensitive values, they should not be in the project's `gradle.properties`,
but rather in the user's `~/.gradle/gradle.properties`.

```
github.credentials.username = username
github.credentials.password = password
```

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

---

## Release Notes

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

