# gradle-git

A set of plugins to support Git in the Gradle build tool.

Credit goes to [Peter Ledbrook](https://github.com/pledbrook) for the initial idea.

---

## Adding the Plugins

Add the following line to your build to use the gradle-git plugins.

    buildscript {
      repositories { mavenCentral() }
      dependencies { classpath 'org.ajoberstar:gradle-git:0.1.0' }
    }

## Git Plugin

The `git` plugin can be applied as follows:

    apply plugin: 'git'

This only adds a `git` extension object.

## Github Plugin

The `github` plugin also applies the `git` plugin.

    apply plugin: 'github'

This merely adds the `github` extension object.  Credentials for
Github can be specified in the `gradle.properties` file.

    github.credentials.username = username
    github.credentials.password = password

The Github repository can be specified using the extension:

    github.repoUri = 'https://ajoberstar@github.com/ajoberstar/gradle-git.git'

SSH connections are currently not supported.

## Gh-Pages Plugin

The `gh-pages` plugin also applies the `github` plugin.

    apply plugin: 'gh-pages'

This configures tasks needed to clone, add, commit, and push changes to the gh-pages branch
of your Github repository.

The files that will be published to gh-pages are in the `github.ghpages.distribution` CopySpec.
By default all files in `src/main/ghpages` will be included.

The default location the repository will be cloned to is `build/ghpages`.  This can be configured
with `github.ghpages.destinationPath`.

---

## Release Notes

**v0.1.0**

Initial release.
