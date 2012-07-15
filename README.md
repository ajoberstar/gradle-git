# gradle-git

A set of plugins to support Git in the Gradle build tool.

Credit goes to [Peter Ledbrook](https://github.com/pledbrook) for the initial idea.

**API Documentation:**

* [Javadoc](http://ajoberstar.org/gradle-git/docs/javadoc)
* [Groovydoc](http://ajoberstar.org/gradle-git/docs/groovydoc)

---

## Adding the Plugins

Add the following line to your build to use the gradle-git plugins.

    buildscript {
      repositories { mavenCentral() }
      dependencies { classpath 'org.ajoberstar:gradle-git:0.1.2' }
    }

## Git Tasks

If all you want to do is use a few of the tasks, there is no need to apply
any of the plugins (they do need to be on the classpath as described above).
You merely need to start using the tasks:

```groovy
task tag(type: org.ajoberstar.gradle.git.tasks.GitTag) {
	tagName = version
	message = "Release of ${version}"
}
```

By default tasks will act on the repository in the root project's directory.

## Git Plugin

The `git` plugin can be applied as follows:

    apply plugin: 'git'

This only adds a `git` extension object.

## Github Plugin

The `github` plugin also applies the `git` plugin.

    apply plugin: 'github'

This merely adds the `github` extension object.  Credentials for
Github can be specified in the `gradle.properties` file.  As these are
sensitive values, they should not be in the project's `gradle.properties`,
but rather in the user's `~/.gradle/gradle.properties`.

    github.credentials.username = username
    github.credentials.password = password

The Github repository can be specified using the extension.  This is only
used when cloning the repository.

    github.repoUri = 'https://ajoberstar@github.com/ajoberstar/gradle-git.git'

## Gh-Pages Plugin

The `gh-pages` plugin also applies the `github` plugin.

    apply plugin: 'gh-pages'

This configures tasks needed to clone, add, commit, and push changes to the gh-pages branch
of your Github repository.

The files that will be published to gh-pages are in the `github.ghpages.distribution` CopySpec.
By default all files in `src/main/ghpages` will be included.

The default location the repository will be cloned to is `build/ghpages`.  This can be configured
with `github.ghpages.destinationPath`.

To publish your changes run:

    ./gradlew publishGhPages

---

## Release Notes

**v0.1.2**

* Added support for SSH connections with the help of [Urs](https://github.com/UrsKR)

**v0.1.1**

* Added GitTag task contributed by [Urs Reupke](https://github.com/UrsKR).
* The `repoPath` for all Git tasks is defaulted to the root project's directory.

**v0.1.0**

Initial release.
