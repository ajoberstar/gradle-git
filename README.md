# gradle-git

A set of plugins to support Git in the Gradle build tool.

[![Build Status](https://travis-ci.org/ajoberstar/gradle-git.png?branch=master)](https://travis-ci.org/ajoberstar/gradle-git)

There are three primary use cases for these plugins:
* Publishing to a Github Pages website.  The `org.ajoberstar.github-pages` plugin adds support
for publishing static files to the `gh-pages` branch of a Git repository.
* Managing your release process. The `org.ajoberstar.grgit-release` plugin adds an opinionated
way to manage releases and comply with [Semantic Versioning](http://semver.org).
* General Git actions.  This plugin JAR depends on, and makes available,
[grgit](https://github.com/ajoberstar/grgit) which provides a Groovy API for
interacting with Git repositories.

For more information see the documentation in the next sections.

**API Documentation:**

* [Javadoc](http://ajoberstar.org/gradle-git/docs/javadoc)
* [Groovydoc](http://ajoberstar.org/gradle-git/docs/groovydoc)

Credit goes to [Peter Ledbrook](https://github.com/pledbrook) for the initial
idea for the `org.ajoberstar.github-pages` plugin.

Thanks to [Zafar Khaja](https://github.com/zafarkhaja) for the very helpful
[java-semver](https://github.com/zafarkhaja/java-semver) library.

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

See the Release Notes at the bottom of this README for the available versions.

## Using Grgit

For freeform access to a Git repository, you only need the dependency on
`gradle-git`, no additional plugins need to be applied. Import the `grgit`
package and start using a repository.

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
Examples of how to use each operation are provided there.

### Authentication

Grgit provides multiple ways to authenticate with remote repositories. If using hardcoded credentials, it is suggested to use the system properties rather than including them in the repository.

See Grgit's [AuthConfig](http://ajoberstar.org/grgit/docs/groovydoc/org/ajoberstar/grgit/auth/AuthConfig.html) docs for information on the
various authentication options.

Methods that produce a `Grgit` instance, which includes `clone`, `init`, and `open`, all take a `Credentials` instance if
you want to provide hardcoded credentials programmatically.

## org.ajoberstar.github-pages

```groovy
apply plugin: 'org.ajoberstar.github-pages'

githubPages {
  repoUri = '...'
  pages {
    from javadoc.outputs.files
  }
}
```

See the [GithubPagesPluginExtension](http://ajoberstar.org/gradle-git/docs/groovydoc/org/ajoberstar/gradle/git/ghpages/GithubPagesPluginExtension.html) docs for information on the defaults.

The plugin adds a single `publishGhPages` task that will clone the
repository, copy in the files in the `pages` `CopySpec`, add all of
the changes, commit, and push back to the remote.

## org.ajoberstar.grgit-release

### Other Options for Gradle Release Plugins

Before describing this plugins functionality I want to point out the other plugins available (that I'm aware of):

* [townsfolk/gradle-release](https://github.com/townsfolk/gradle-release) - designed to work like the Maven Release
plugin. If that's what you're looking for this is your best option.
* [ari/gradle-release-plugin](https://github.com/ari/gradle-release-plugin/) - Seems purely oriented around versioning
the release and tagging it. Versions are generated dynamically based on current branch or tag.
* [stianh/gradle-release-plugin](https://github.com/stianh/gradle-release-plugin) - Similar approach (and created before)
the `ari` plugin. However, as of `4/13/14` it hasn't had any commits in 9 months.

### Approach of org.ajoberstar.grgit-release

The `org.ajoberstar.grgit-release` plugin is intended to be opinionated, and I am not trying to meet all needs. The core principles of
this plugin are:

* [Semantic versioning](http://semver.org) is the ideal way to version code (for most use cases).
* A Git repository contains enough information to be able to build useful version numbers that comply with semver, with
limited direction from the user. **The version should not need to be hardcoded into the source code.**

The general functionality involves:

1. Inferring the desired version based on the nearest tagged version(s) (similar to `git describe`) and the users input
of the **scope** of the changes being made and what **stage** of development they are in.
1. Ensuring all changes in the repository have been committed.
1. Ensuring there aren't any commits in the upstream branch that haven't been merged yet.
1. Validating that there have been commits since the nearest version. (Don't re-release with a different version.)
1. Optionally, checking all Java/Groovy files for @since tags. If they exist, ensure that they match a version that has
been tagged in the repository or the version that was just inferred.
1. Execute whatever release tasks the user specified in the plugin configuration. (e.g. `build`, `publishToRepo`)
1. Optionally, tag the release. Can be configured to only tag certain types of versions. (e.g. only `final` and `rc`)
1. Push changes in current branch to the remote. If a tag was made it will also be pushed.

### Applying the Plugin

Only the `grgit` property is mandatory. `releaseTasks` should be filled in if
you want the code built or published. Everything else has defaults, as noted
below.

```groovy
apply plugin: 'org.ajoberstar.grgit-release'

import org.ajoberstar.grgit.*

release {
  grgit = Grgit.open(project.file('.'))
  remote = 'upstream' // default is 'origin'
  prefixTagNameWithV = false // default is true
  releaseTasks = ['build', 'publishGhPages', 'bintrayUpload'] // defaults to []
  enforceSinceTags = true // default is false

  version {
    untaggedStages = ['alpha'] // default is ['dev']
    taggedStages = ['beta'] // default is ['milestone', 'rc']
    useBuildMetadataForStage = { true } // default is { stage -> stage != 'final' }
    createBuildMetadata = { new Date().format('yyyy.MM.dd.hh.mm.ss') } // default is { grgit.head().abbreviatedId }
  }

  generateTagMessage = { version -> // default is "Release of ${version}"
    StringBuilder builder = new StringBuilder()
    builder.append('Release of ')
    builder.append(version)
    builder.append('\n\n')
    grgit.log {
      range "v${version.nearest.normal.toString()}^{commit}", 'HEAD'
    }.inject(builder) { bldr, commit ->
      bldr.append('- ')
      bldr.append(commit.shortMessage)
      bldr.append('\n')
    }
    builder.toString()
  }
}
```

### Using the Plugin

The two main concepts used as part of the version inference are:

* A **normal** version in the parlance of semver is `<major>.<minor>.<patch>` without
any pre-release info or build metadata.
* The **nearest** version is determined by finding the shortest commit log between
a tagged version and the current `HEAD`. In cases where there are multiple version
tags with the same distance, they will be sorted according to semver rules and the
one with the highest precedence will be returned.
* **scope** - Changes can be either `MAJOR`, `MINOR`, or `PATCH`. These correspond
to the portions of a semver version `<major>.<minor>.<patch>[.<pre-release>][+<build-metadata>]`
* **stage** - Correspond to stages of development. The entirety of available stages is
the union of `untaggedStages` and `taggedStages` with the addition of `final`. Stages will be
used as part of the pre-release info in a version: `<stage>.<num>`.
  * For untagged stages the `num` will correspond to the number of commits since the nearest
  tagged normal version.
  * For tagged stages the `num` will be incremented from the nearest version if it
  has the same normal component and stage. Otherwise it will be set to 1.

The version is determined using two project properties:

* `release.scope` - defaults to the `patch` if none specified.
* `release.stage` - defaults to the first entry in `untaggedStages`.
** Given `untaggedStages` is a `SortedSet` and semver dictates lexicographical sorting
of non-numeric components, this should be the stage with the lowest precedence.

There are 2 tasks that are added to handle releases:
* `prepare` This will run before any other tasks in the build.
  * If there have been no commits since the nearest version the task will fail.
  * Checks for changes in the repository. If there are any changes they will be
  printed out and the task will fail.
  * Fetches from remote.
  * Checks current branch's tracking status to ensure it isn't behind. If it is
  behind the task will fail.
* `release` - This will run after any tasks specified in `releaseTasks` and the `prepare` task.
  * Tags the version if `stage` is either `final` or in `taggedStages`.
  * Pushes the current branch and, if created, the release tag to the remote.

#### Example Usage

* `./gradlew release -Prelease.scope=major -Prelease.stage=final`
* `./gradlew release -Prelease.stage=rc` - default to `patch` scope
* `./gradlew release -Prelease.scope=minor` - default to `dev` stage
* `./gradlew release` - default to `patch` scope and `dev` stage

The release task does not have to be included for version inference to occur.

NOTE: If preferred, the `release.scope` and/or `release.stage` can be versioned
in the project's `gradle.properties`.

### Version Inference

The version will be inferred as soon as `toString()` is called on the version,
which should correspond to the first usage of the version. It will be inferred
using the `release.scope` and `release.stage` properties, or their corresponding
defaults.

When a version is being inferred the first step is to find the nearest tagged version.

1. All tags in the repository are listed.
  * If the name cannot be parsed as a valid semver version, it is ignored. Prefixed `v`s will be removed.
  * If the tag is not an ancestor of `HEAD`, it is ignored.
1. A commit log is generated between the tag and `HEAD`.
1. The tag with the smallest log is returned.
  * In cases where there are multiple tags with the smallest log, they will be ordered
  according to semver rules and the one with the highest precedence will be returned.

This is completed to determine both the absolute nearest version and the nearest normal
version.

The normal component of the inferred version starts from the nearest
normal version and the component corresponding to **scope** is incremented.

e.g. If the nearest normal version is `1.3.2` and the absolute nearest version is
`1.4.0-milestone.2`. Assume the stage is `rc` in all cases.

| Scope   | Inferred Version |
| ------  | ---------------- |
| `PATCH` | `1.3.3-rc.1`     |
| `MINOR` | `1.4.0-rc.1`     |
| `MAJOR` | `2.0.0-rc.1`     |

The pre-release component is based on the requested `stage`. Untagged stages
are numbered by the count of commits since the nearest normal version. Tagged
stages are numbered in incrementing order within a stage.

e.g. If the nearest normal version is `1.3.2`, the absolute nearest version is
`1.4.0-milestone.2`, and there have been 6 commits since `1.3.2`. Assume
the scope is `MINOR` in all cases.

| Stage       | Inferred Version    |
| ----------- | ------------------- |
| `dev`       | `1.4.0-dev.6`       |
| `milestone` | `1.4.0-milestone.3` |
| `rc`        | `1.4.0-rc.1`        |
| `final`     | `1.4.0`             |

The build-metadata is determined based on `useBuildMetadataForStage` and
`createBuildMetadata`. If any is created it is appended to the version after
a `+`. For example, with the defaults: `1.2.3-rc.1+123abcd`

### Validating @since Tags in Source Code

An additional task (`validateSinceTags`) is added to enforce `@since` tags in
Javadoc/Groovydoc comments. It defaults to checking all `.java` or `.groovy` files
in the `main` source set. This task only runs if `release.enforceSinceTags` is set
to `true`. By default this task will finalize the `read*` tasks.

The task will enforce that any `@since` tags used in the source code correspond to
a version considered valid in the repository.

For example, if the repository has the following tags:
* v0.1.0
* 0.2.1+gibberish
* nonsense (for the purposes of version inference this wouldn't be considered, but
it is here)

Also assume that the user executed `release -Prelease.scope=minor -Prelease.stage=rc`.
The only allowed values for `@since` tags would be:
* 0.1.0
* 0.2.1+gibberish (`v` prefixes should not be used)
* nonsense
* 0.3.0 (can use the target normal version)
* 0.3.0-rc.1 (or the entire inferred version)

```groovy
validateSinceTags {
  source += sourceSets.test.allJava // defaults to sourceSets.main.allJava
}
```

## Travis CI Authentication Notes

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

**v0.10.0**

* Primary goal of this release is to get the plugin into the new [Gradle plugin portal](http://plugins.gradle.org).
  * **Breaking change:** Plugin ID's changed from `github-pages` to `org.ajoberstar.github-pages` and `grgit-release` to `org.ajoberstar.grgit-release`.
  * **Possibly breaking change:** Built against Gradle 2.1. This may break compatibility with earlier Gradle versions.
* **Possibly breaking change:** `github-pages` will now do a full sync of the content. If you were relying on it leaving some of the files
outside of your `githubPages.pages` definition alone, this will cause problems for you.
* Allow custom logic for determining if a version is releasable. (#59, courtesy of [Benjamin Muschko](https://github.com/bmuschko))
* No longer throwing `IllegalStateException` when getters on `InferredVersion` are used before the version is inferred.

NOTE: I'm planning on completely rewriting the `grgit-release` plugin for the next release in order to provide more flexibility for
people with other needs.

**v0.9.0**

* Breaking change for the `grgit-release` plugin.
  * The `ready<Scope>As<Stage>` and `release<Scope>As<Stage>` tasks were replaced
by a `prepare` and a `release` task.
  * Scope and stage are now provided by project properties `release.scope` and
`release.stage`.
  * Version inference happens when `toString` is called, instead of when the task
graph is ready.
  * These changes work around some eager evaluation of the version experienced
with the `maven-publish` and some other plugins.

**v0.8.0**
* Updated `GithubPagesPluginExtension` to allow configuration of commit message
used.
* Added an opinionated release plugin `grgit-release`. See docs above for info.

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

