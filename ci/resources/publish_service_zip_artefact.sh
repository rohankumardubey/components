#!/usr/bin/env bash

#
# Copyright (C) 2010-2021 Talend Inc. - www.talend.com
#
# This source code is available under agreement available at
# https://github.com/Talend/apimgmt-misc/blob/master/LICENSE
#
# You should have received a copy of the agreement
# along with this program; if not, write to Talend SA
# 9 rue Pages 92150 Suresnes, France
#

set -xe

# Publish a TCOMPv0 service zip to Nexus
# $1: The version of the artifact to be pushed
# $2: The path to the maven settings file, take it from the appropriate Jenkins credentials
main() {
  local nexusRepositoryId="${1:?Missing nexus repository id}"
  local nexusRepositoryBaseUrl="${2:?Missing nexus repository url}"
  local projectVersion="${3:?Missing project version}"
  local mavenSettingsPath="${4:?Missing maven settings path}"

  local tdpClassifier="tdp"
  local zipName="components-api-service-rest-all-components-${projectVersion}-${tdpClassifier}.zip"

  (
    cd services/components-api-service-rest-all-components

    printf 'Deploying zip %s to repository %s (with %s classifier)\n' "${zipName}" "${nexusRepositoryId}" "${tdpClassifier}"

    mvn deploy:deploy-file \
      --batch-mode \
      --define "groupId=org.talend.components" \
      --define "artifactId=components-api-service-rest-all-components" \
      --define "repositoryId=${nexusRepositoryId}" \
      --define "url=${nexusRepositoryBaseUrl}/${nexusRepositoryId}" \
      --define "version=${projectVersion}" \
      --define 'generatePom=true' \
      --define "file=target/$zipName" \
      --define 'packaging=zip' \
      --define "classifier=$tdpClassifier" \
      --define "build-time=${BUILD_TIMESTAMP}" \
      --define "build-branch=$(git rev-parse --abbrev-ref HEAD)" \
      --define "build-commit=$(git log --max-count '1' --pretty=format:%H)" \
      --settings "$mavenSettingsPath"
  )
}

main "$@"
