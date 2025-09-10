---
layout: default
title: Releasing Truth
---

* auto-gen TOC:
{:toc}

## Publish the release's Javadocs

To do so, run our Javadoc script:

```sh
git pull --tags

git checkout v${NEW_VERSION?}

RELEASE_VERSION=${NEW_VERSION?} ./util/generate-latest-docs.sh
```

This will check out the gh-pages branch into a new subfolder, run the aggregated
Javadocs (one Javadoc for the core plus extensions) and publish them into a
version-specific subfolder. They are then ready to be referenced by the main
documentation site.

## Edit the release info pinned to the git tag in GitHub

Once you have pushed the tag, go to https://github.com/google/truth/tags, click
the new tag, and click the "Create release from tag" button.

Add in any changelog information for that release, and clean up the title, which
will, by default, simply be the tag name. Make it all pretty. Once the *Maven*
release appears in
https://repo1.maven.org/maven2/com/google/truth/truth/maven-metadata.xml, it's
time to save your work, and the release will now be announced and nicely
documented.

> ***Note:*** Under the previous release, a link saying *x commits to master
> since this release"* will take you to a handy filtered list of commits which
> can be used as a basis for figuring out the release notes.

## Post-release

### Update version numbers in docs

Create a commit that updates the `_config.yml` file with the new version number.
