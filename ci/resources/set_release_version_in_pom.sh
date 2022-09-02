#!/usr/bin/env bash

set -xe

# Changes the version in a pom and its children
# $1: The new version
# $2: The path to the maven settings file, take it from the appropriate Jenkins credentials
main() {
  local newVersion="${1:?Missing new version}"
  local mavenSettingsPath="${2:?Missing maven settings path}"

  mvn versions:set \
    --batch-mode \
    --define 'generateBackupPoms=false' \
    --define "newVersion=${newVersion}" \
    --settings "${mavenSettingsPath}"
}

main "$@"
