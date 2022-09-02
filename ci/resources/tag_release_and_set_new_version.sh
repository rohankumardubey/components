#!/usr/bin/env bash

set -xe

# Commits and tags the repository
# $1: The project version which is released
# $2: The path to the maven settings file, take it from the appropriate Jenkins credentials
# $3: The new version to bump to
main() {
  local projectVersion="${1:?Missing project version}"
  local projectNextVersion="${2?Missing maven next version}"
  local mavenSettingsPath="${3:?Missing maven settings path}"

  commitPom ":construction: Release version ${projectVersion}" '#release'

  local releaseTagName="tdp/release/${projectVersion}"
  git tag "${releaseTagName}"

  mvn versions:set \
    --batch-mode \
    --define 'generateBackupPoms=false' \
    --define "newVersion=${projectNextVersion}" \
    --settings "${mavenSettingsPath}" \
    --activate-profiles "${mavenProfiles}"

  commitPom ':construction: Bump version'

  git push origin "$(git rev-parse --abbrev-ref HEAD)"
  git push origin "${releaseTagName}"
}

commitPom() {
  local commitMessage="$1"
  local commitBody="$2"

  git status --short \
    | grep 'pom.xml$' \
    | awk '{print $2}' \
    | while read -r file; do git add "${file}"; done

  if ! git diff --cached --exit-code &> /dev/null; then
    # Only commit if there's something to commit
    git commit \
      --message "${commitMessage}" \
      --message "${commitBody}"
  fi
}

main "$@"
