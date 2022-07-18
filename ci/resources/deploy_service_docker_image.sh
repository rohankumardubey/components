#!/usr/bin/env bash

set -xe

# Builds & deploys TCOMPv0 Docker image and pushes it on Talend's registry
# Two tags are set, the version's and latest
# $1: The host of the local Docker registry where the Docker image has been built
# $2: The host of the Talend Docker registry
# $3: The project's version
main () {
  local localRegistryHost="${1:?Missing local registry host}"
  local talendRegistryHost="${2:?Missing talend registry host}"
  local projectVersion="${3:?Missing project version}"

  local dockerImageName="tcomp-components-api-service-rest-all-components"
  local dockerImageTimestamp=$(date '+%Y%m%d-%H%M%S')

  # Format already used by the fabric8 docker-maven-plugin configuration
  local dockerImageTag="${projectVersion}-${dockerImageTimestamp}"

  (
    cd services/components-api-service-rest-all-components

    mvn package \
    --define 'skipTests=true' \
    --define "docker.image.tag=${dockerImageTag}" \
    --activate-profiles docker

    printf "Successfully built image %s:%s" "${dockerImageName}:${dockerImageTag}"

    printf "Docker tag & push to %s\n" "${talendRegistryHost}"
    docker tag  "${localRegistryHost}/${dockerImageName}:${dockerImageTag}" "${talendRegistryHost}/${dockerImageName}:${dockerImageTag}"
    docker push "${talendRegistryHost}/${dockerImageName}:${dockerImageTag}"

    docker tag "${localRegistryHost}/${dockerImageName}:${dockerImageTag}" "${talendRegistryHost}/${dockerImageName}:latest"
    docker push "${talendRegistryHost}/${dockerImageName}:latest"
  )
}

main "$@"
