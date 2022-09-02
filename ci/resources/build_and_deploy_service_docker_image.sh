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
  local dockerImageName=${3?Missing docker image name}
  local dockerImageTag="${4:?Missing docker image tag}"
  local mavenSettingsPath="${5:?Missing maven settings path}"

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

  # Define a git tag with the same name as the Docker tag for tracability reasons (even for non-release builds)
  local dockerTracabilityTagName="tdp/docker/${projectVersion}"
  git tag "${dockerTracabilityTagName}"
  git push origin "${dockerTracabilityTagName}"
}

main "$@"
