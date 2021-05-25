#!/bin/bash
# A script to automatically deploy javadocs.
#
# This script is useful both in a CI regular build, where it will generate
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
set -euE

echo -e "Publishing javadoc...\n"

if [ -n "${RELEASE_VERSION:-}" ]; then
  # Release
  version_subdir=api/${RELEASE_VERSION}
  commit_message="Release $RELEASE_VERSION javadoc pushed to gh-pages."
  github_url="git@github.com:google/truth.git"
else
  # CI
  version_subdir=api/latest
  commit_message="Latest javadoc on successful CI build auto-pushed to gh-pages."
  github_url="https://x-access-token:${GITHUB_TOKEN}@github.com/google/truth.git"
fi

mvn javadoc:aggregate
perl -ni -e 'print unless /Tolerant.*Comparison/ || /SubjectBuilderCallback/ || /UsingCorrespondence/ || /AsIterable/ || /Correspondence[.][A-Z]/ || /FluentAssertion/ || /PathSubject/ || /Re2jSubjects/ || /Ordered/' target/site/apidocs/allclasses-frame.html
find target/site/apidocs -name '*.html' | xargs perl -077pi -e 's#<li class="blockList"><a name="nested.classes.inherited.from.class.com.google.common.truth.\w*Subject">.*?</li>##msg; if (m#<!-- ======== NESTED CLASS SUMMARY ======== -->(.*?)(?=<!-- =)#ms) { if ($1 !~ m#nested.classes.inherited.from|memberSummary#) { s#<!-- ======== NESTED CLASS SUMMARY ======== -->.*?(?=<!-- =)##msg; } }'
target_dir="$(pwd)/target"
cd ${target_dir}
rm -rf gh-pages
git clone --quiet --branch=gh-pages "${github_url}" gh-pages > /dev/null
cd gh-pages

if [[ -z "${RELEASE_VERSION:-}" ]]; then
  git config --global user.name "$GITHUB_ACTOR"
  git config --global user.email "$GITHUB_ACTOR@users.noreply.github.com"
fi
api_version_dir="${target_dir}/gh-pages/${version_subdir}"
git rm -rf ${api_version_dir} || true
cp -ar ${target_dir}/site/apidocs ${api_version_dir}
git add -A -f ${api_version_dir}
git commit -m "${commit_message}"
git push -fq origin gh-pages > /dev/null

echo -e "Published Javadoc to gh-pages.\n"
