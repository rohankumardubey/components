#!/usr/bin/env bash

set -xe

# Logins to the Talend Docker registry
# $1: The Talend Docker registry host, take it from the Jenkins global variables
# $2: The login for artifactory
# $3: The password for artifactory
main () {
  local artifactoryRegistryHost="${1:?Missing artifactory registry host}"
  local artifactoryLogin="${2:?Missing artifactory login environment}"
  local artifactoryPassword="${3:?Missing artifactory password environment}"

  printf "Docker Login to %s\n" "${artifactoryRegistryHost}"
  docker login "${artifactoryRegistryHost}" \
    --username "${artifactoryLogin}" \
    --password-stdin <<< "${artifactoryPassword}"
}

main "$@"
