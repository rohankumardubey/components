#!/usr/bin/env bash

set -xe

# Builds & deploys TCOMPv0 Docker image and pushes it on Talend's registry
# Two tags are set, the version's and latest
# $1: The host of the Talend Docker registry
# $2: The project's version
# $3: The path to the maven settings file
main () {
  local talendRegistryHost="${1:?Missing talend registry host}"
  local projectVersion="${2:?Missing project version}"
  local mavenSettingsPath="${3:?Missing maven settings path}"

  local dockerImageName="tcomp-components-api-service-rest-all-components"
  local dockerImageTimestamp=$(date '+%Y%m%d-%H%M%S')

  # Format already used by the fabric8 docker-maven-plugin configuration
  local dockerImageTag="${projectVersion}-${dockerImageTimestamp}"

  (
    cd services/components-api-service-rest-all-components

    # Service image is directly built as "${talendRegistryHost}/${dockerImageName}" thanks to the docker-maven-plugin
    # configuration. Probably not ideal...
    mvn package \
    --define 'skipTests=true' \
    --define "docker.image.tag=${dockerImageTag}" \
    --activate-profiles docker

    printf "Successfully built image %s:%s\n" "${dockerImageName}" "${dockerImageTag}"

    printf "Docker tag & push to %s\n" "${talendRegistryHost}"
    docker tag  "${talendRegistryHost}/${dockerImageName}:${dockerImageTag}" "${talendRegistryHost}/${dockerImageName}:${dockerImageTag}"
    docker push "${talendRegistryHost}/${dockerImageName}:${dockerImageTag}"

    docker tag "${talendRegistryHost}/${dockerImageName}:${dockerImageTag}" "${talendRegistryHost}/${dockerImageName}:latest"
    docker push "${talendRegistryHost}/${dockerImageName}:latest"
  )
}

main "$@"
