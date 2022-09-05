#!/usr/bin/env bash

set -xe

# Creates a ~/.netrc file with GitHub credentials set to connect to GitHub and its REST API
# $1: The git login
# $2: The git password
# $3: The path to the file where the CI git exclusions are declared.
#     Avoids accidentally committing CI-only files.
main () {
  local gitLogin="${1:?Missing git login}"
  local gitPassword="${2:?Missing git password}"

  {
    printf "machine github.com\n"
    printf "login %s\n" "${gitLogin}"
    printf "password %s\n" "${gitPassword}"

    printf "machine api.github.com\n"
    printf "login %s\n" "${gitLogin}"
    printf "password %s\n" "${gitPassword}"
  } > ~/.netrc

  chmod 600 ~/.netrc
}

main "$@"
