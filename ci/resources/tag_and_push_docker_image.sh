#!/usr/bin/env bash

set -xe

# Tags the built Docker image and pushes it on Talend's registry
# Two tags are set, the version's and latest
# $1: The host of the local Docker registry where the Docker image has been built
# $2: The host of the Talend Docker registry
# $3: The name of the Docker image built
# $4: The tag (version) of the Docker image built
main () {
  local localRegistryHost="${1:?Missing local registry host}"
  local talendRegistryHost="${2:?Missing talend registry host}"
  local dockerImageName="${3:?Missing docker image name}"
  local dockerImageTag="${4:?Missing docker image tag}"

  printf "Docker tag & push to %s\n" "${talendRegistryHost}"
  docker tag  "${localRegistryHost}/${dockerImageName}:${dockerImageTag}" "${talendRegistryHost}/${dockerImageName}:${dockerImageTag}"
  docker push "${talendRegistryHost}/${dockerImageName}:${dockerImageTag}"

  docker tag "${localRegistryHost}/${dockerImageName}:${dockerImageTag}" "${talendRegistryHost}/${dockerImageName}:latest"
  docker push "${talendRegistryHost}/${dockerImageName}:latest"
}

main "$@"
