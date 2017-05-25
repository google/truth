#!/bin/bash
# A script to automatically deploy javadocs.
#
# This script is useful both in a travis-ci.org regular build, where it will generate
# javadocs (aggregated) via a maven build, and deploy them to github pages. This script
# is derived from instructions given in this blog article:
# http://benlimmer.com/2013/12/26/automatically-publish-javadoc-to-gh-pages-with-travis-ci/
#
# If the RELEASE_VERSION environment variable is set, then it will perform a similar
# action, but push to a versioned api docs folder, under the current user's credentials
# rather than using an encrypted secret via github's GH_TOKEN mechanism.  Users who
# use the script this way must have a .ssh key which they have declared on github.com
# per the instructions here: https://help.github.com/articles/generating-ssh-keys/
#
readonly GH_PROJECT=truth
readonly ORG=google
readonly EXPECTED_REPO_SLUG="${ORG}/${GH_PROJECT}"

if [[ -n "$RELEASE_VERSION" || \
    "$TRAVIS_REPO_SLUG" == "$EXPECTED_REPO_SLUG" && \
    "$TRAVIS_JDK_VERSION" == "$JDK_FOR_PUBLISHING" && \
    "$TRAVIS_PULL_REQUEST" == "false" && \
    "$TRAVIS_BRANCH" == "master" ]]; then
  echo -e "Publishing javadoc...\n"

  if [ -n "$RELEASE_VERSION" ]; then
    # Release
    version_subdir=api/${RELEASE_VERSION}
    github_url="git@github.com:${EXPECTED_REPO_SLUG}.git"
    commit_message="Release $RELEASE_VERSION javadoc pushed to gh-pages."
  else
    # Travis
    version_subdir=api/latest
    github_url="https://${GH_TOKEN}@github.com/${EXPECTED_REPO_SLUG}"
    commit_message="Lastest javadoc on successful travis build $TRAVIS_BUILD_NUMBER auto-pushed to gh-pages."
  fi

  mvn javadoc:aggregate
  target_dir="$(pwd)/target"
  cd ${target_dir}
  git clone --quiet --branch=gh-pages ${github_url} gh-pages > /dev/null
  cd gh-pages

  if [[ -z "$RELEASE_VERSION" ]]; then
    git config --global user.email "travis@travis-ci.org"
    git config --global user.name "travis-ci"
  fi
  api_version_dir="${target_dir}/gh-pages/${version_subdir}"
  git rm -rf ${api_version_dir}
  mv ${target_dir}/site/apidocs ${api_version_dir}
  git add -A -f ${api_version_dir}
  git commit -m "${commit_message}"
  git push -fq origin gh-pages > /dev/null

  echo -e "Published Javadoc to gh-pages.\n"
fi
