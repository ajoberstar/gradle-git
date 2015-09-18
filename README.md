# gradle-git

[![Bintray](https://img.shields.io/bintray/v/ajoberstar/gradle-plugins/org.ajoberstar%3Agradle-git.svg?style=flat-square)](https://bintray.com/ajoberstar/gradle-plugins/org.ajoberstar%3Agradle-git/_latestVersion)
[![Travis](https://img.shields.io/travis/ajoberstar/gradle-git.svg?style=flat-square)](https://travis-ci.org/ajoberstar/gradle-git)
[![Maintainer Status](http://stillmaintained.com/ajoberstar/gradle-git.svg)](http://stillmaintained.com/ajoberstar/gradle-git)
[![GitHub license](https://img.shields.io/github/license/ajoberstar/gradle-git.svg?style=flat-square)](https://github.com/ajoberstar/gradle-git/blob/master/LICENSE)

## Why do you care?

Git is immensely popular and being able to interact with it as part of a build process can be very valuable
to provide a more powerful and consistent result.

## What is it?

gradle-git is a set of [Gradle](http://gradle.org) plugins:

* `org.ajoberstar.grgit` - provides a `Grgit` instance, allowing interaction with the Git repository
the Gradle project is contained in
* `org.ajoberstar.github-pages` - publishes files to the `gh-pages` branch of a Github repository
* `org.ajoberstar.release-base` - general structure for inferring a project version and releasing it
* `org.ajoberstar.release-opinion` - opinionated defaults for `org.ajoberstar.release-base`

See [Grgit](https://github.com/ajoberstar/grgit) for details on the Git library used underneath, including
configuration for authentication.

## Usage

**NOTE:** gradle-git modules require Java 7 (or higher).

* [Release Notes](https://github.com/ajoberstar/gradle-git/releases)
* [Wiki](https://github.com/ajoberstar/gradle-git/wiki)
* [Javadoc](http://ajoberstar.org/gradle-git/docs/javadoc)
* [Groovydoc](http://ajoberstar.org/gradle-git/docs/groovydoc)

## Questions, Bugs, and Features

Please use the repo's [issues](https://github.com/ajoberstar/gradle-git/issues)
for all questions, bug reports, and feature requests.

## Contributing

Contributions are very welcome and are accepted through pull requests.

Smaller changes can come directly as a PR, but larger or more complex
ones should be discussed in an issue first to flesh out the approach.

If you're interested in implementing a feature on the
[issues backlog](https://github.com/ajoberstar/gradle-git/issues), add a comment
to make sure it's not already in progress and for any needed discussion.

## Acknowledgements

Thanks to all of the [contributors](https://github.com/ajoberstar/gradle-git/graphs/contributors).

Credit goes to [Peter Ledbrook](https://github.com/pledbrook) for the initial
idea for the `org.ajoberstar.github-pages` plugin.

Thanks to [Zafar Khaja](https://github.com/zafarkhaja) for the very helpful
[java-semver](https://github.com/zafarkhaja/jsemver) library.
