#!/bin/bash
set -euo pipefail

export TERM=dumb

if [ "${TRAVIS_BRANCH}" == "master" ] && [ "$TRAVIS_PULL_REQUEST" == "false" ]; then
	echo 'Running Gradle check with SonarQube analysis'
	./gradlew clean check sonarqube \
		-Dsonar.host.url=https://sonarqube.ajoberstar.com \
		-Dsonar.login=$SONARQUBE_TOKEN
elif [ "$TRAVIS_PULL_REQUEST" != "false" ] && [ -n "${GITHUB_TOKEN:-}" ]; then
	echo 'Running Gradle check with SonarQube preview for pull requests'
	./gradlew clean check sonarqube \
		-Dsonar.host.url=https://sonarqube.ajoberstar.com \
		-Dsonar.login=$SONARQUBE_TOKEN \
		-Dsonar.github.pullRequest=$TRAVIS_PULL_REQUEST \
        -Dsonar.github.repository=$TRAVIS_REPO_SLUG \
        -Dsonar.github.oauth=$GITHUB_TOKEN \
        -Dsonar.analysis.mode=issues
else
	echo 'Running Gradle check without SonarQube analysis'
	./gradlew clean check
fi
