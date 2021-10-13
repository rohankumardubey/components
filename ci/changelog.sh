#! /bin/bash

# git config hack when pushing to bypass :
# "fatal: could not read Username for 'https://github.com': No such device or address" error.
# This appeared after 2fa auth activation on github.
git config --global credential.username ${GITHUB_LOGIN}
git config --global credential.helper '!echo password=${GITHUB_TOKEN}; echo'
git config --global credential.name "jenkins-build"
env | sort

echo "Release version ${RELEASE_VERSION}"

# Maintenance branch
PATCH=$(($(echo ${RELEASE_VERSION} | cut -d. -f3) - 1))
if [[ $PATCH -gt 0 ]]; then
    MINOR=$(echo ${RELEASE_VERSION} | cut -d. -f2)
else
    PATCH=$(echo '0')
    MINOR=$(($(echo ${RELEASE_VERSION} | cut -d. -f2) - 1))
fi
MAJOR=$(echo ${RELEASE_VERSION} | cut -d. -f1)
PREVIOUS_RELEASE_VERSION=${MAJOR}.${MINOR}.${PATCH}
echo "Previous release version ${PREVIOUS_RELEASE_VERSION}"

echo "Getting last commit hash."
echo "Fetching all tags."
#Too many unnecessary logged info
git fetch --tags -q
LAST_COMMIT_HASH=$(git log --format="%H" release/${PREVIOUS_RELEASE_VERSION}...release/${RELEASE_VERSION} | head -n -1 | tail -n 1)

if [[ -z "${LAST_COMMIT_HASH}" ]]; then
    echo "Cannot evaluate last commit hash. Changelog won't be generated."
else
    echo "Last commit hash - ${LAST_COMMIT_HASH}"
    echo "Draft - ${DRAFT}"

    cd .. && \
    git clone https://github.com/Talend/connectivity-tools.git && \
    cd connectivity-tools/release-notes && \
    mvn clean package

    java -jar target/$(find target -maxdepth 1 -name "*.jar" | cut -d/ -f2) ${LAST_COMMIT_HASH}
fi
