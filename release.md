---
layout: default
title: Releasing Truth
---

* auto-gen TOC:
{:toc}


## Overview

At a high level, the steps involved are as listed in the above table of
contents.  Namely, branch for release, update the versions, push to
sonatype, and finalize the release. Each step has some ideosyncracies, and
follows below.

Any specific version numbers should be assumed to be examples and the real,
current version numbers should be substituted.

## Detail

### Preconditions

***NOTE:*** *These preconditions include important minutia of maven
deployments.  Make sure you read the [OSSRH Guide] and the [Sonatype GPG
blog post][GPG].*

Releases involve releasing to Sonatype's managed repository which backs the
maven central repository.  To push bits to sonatype requires:

  1. an account on oss.sonatype.org
  2. permission for that account to push com.google.truth
  3. a pgp certificate (via gnupg) with a published public key
  4. a [${HOME}/.m2/settings.xml][settings.xml] file containing the credentials
     for the account created in step #1, associated with the server id
     `sonatype-nexus-staging`.  Afterwords, your settings.xml file should look
     something like:

     ```xml
       <settings>
         <servers>
           <server>
             <id>sonatype-nexus-staging</id>
             <username>your_username</username>
             <password>your_password</password>
           </server>
         </servers>
       </settings>
     ```

The administrative steps above are all documented in Sonatype's [OSSRH Guide].
The GPG instructions particular to this process can be found in this [Sonatype
GPG blog entry][GPG].

### Create a release branch

First checkout the main project's master branch, and create a branch on which
to do the release work (to avoid clobbering anything on the master branch):

```shell
git clone git@github.com:google/truth.git truth_release
cd truth_release
git checkout ${SPECIFIC_COMMIT_VERSION} # optional if not HEAD of master branch.
git checkout -b release_truth_version_branch
# cherry-pick anything needed
mvn verify
```

This generates a new branch, and does a full build to ensure that what is
currently at the tip of the branch is sound.

### Update versions

#### Update the project's version.

Update the versions of the project and commit the changes, like so:

```shell
mvn versions:set versions:commit -DnewVersion=${NEW_VERSION}
git commit -am "[release] Release Truth ${NEW_VERSION}"
```

> ***Note:*** Truth and its subprojects are all released as one version.  The
> above command will update all artifacts to ${NEW_VERSION}.  At some point it
> may release some artifacts independently.  At that point these instructions
> should be changed to omit `-DnewVersion` and each module can be set
> individually.

### Tag the release

The release tags simply follow the format `release_MAJ_MIN[_PATCH]` so simply do
this, for instance:

```shell
git tag release_9_45 # fake numbers, change them to the correct ones.
```

### Build and deploy the release to sonatype

A convenience script exists to properly run a standard `mvn deploy` run
(which pushes built artifacts to the staging repository).  It also activates
the release profile which ensures that the GnuPG plugin is run, signing the
binaries per Sonatype's requriements, adds in the generation of -javadoc and
-sources jars, etc.

It's parameter is the label for your GnuPG key which can be seen by running
`gpg --list-keys` which supplies output similar to the following:

```
pub   2048D/D4906B68 2014-12-16
uid                  Christian Edward Gruber (Maven Deployments) <cgruber@google.com>
```

> More detail about GPG and Sonatype repositories [in this blog post][GPG]

Given the above example, you would then run:

```shell
util/mvn-deploy.sh --signing-key D4906B68
```

... and the script will kick off the maven job, pausing when it first needs to
sign binaries to ask for your GnuPG certificate passphrase (if any).  It then
pushes the binaries and signatures up to sonatype's staging repository.

### Perform the release on Sonatype's maven repository manager

Sonatype manages the `repo.maven.org/maven2` "central" repository. You need
to use this tool to perform the final release. If you do not have a sonatype
account and proper access rights, sign up at issues.sonatype.org and file
an issue to add your username to have rights to the `com.google.truth` groupId.

> ***Note:*** See [preconditions](#preconditions) above for more detailed setup.

#### Verify the release

Log in to [oss.sonatype.org][OSS] and select "Staging repositories".  In the
main window, scroll to the botton where a staging repository named roughly
after the groupId (com.google.truth) will appear.

> ***Note:*** *while this can be inspected, Sonatype performs several checks
> automatically when going through the release lifecycle, so generally it is
> not necessary to further inspect this staging repo.*

Select the repository.  You can check to ensure it is the correct repository by
descending the tree in the lower info window.  If you are convinced it is the
correct one, click on the `close` button (in the upper menu bar) and optionally
enter a message (which will be included in any notifications people have set
up on that repository).  Wait about 60 seconds or so and refresh.  If it's not
done, just wait another 60 seconds and refresh.

If successful, the `release` button will be visible.

#### What if it goes wrong?

If sonatype's analysis has rejected the release, you can check the information
in the lower info window to see what went wrong.  Failed analyzes will show
in red, and the problem should be remedied and step #3 (Tag the release) should
be re-attempted with `tag -f release_MAJ_MIN` once the fixes have been
committed.  Then subsequent steps repeated.

### Release the artifact to the public repository

Assuming sonatype's validation was successful, press the `release` button,
fill in the optional message, and the repository will be released and
automatically dropped once its contents have been copied out to the master
repository.

At this point, the maven artifact(s) will be available for consumption by
maven builds within a few minutes (though it will not be present on
<http://search.maven.org> for about an hour).  You can confirm that the
artifacts have been pushed (even if they're not searchable) by going to
<https://repo1.maven.org/maven2/com/google/truth/truth/>. Once the new version
number appears as a folder, the new version is accessible to maven (and other)
builds.

### Push the tag to github

Since the release was committed to the maven repository, the exact project
state used to generate that should be marked.  To push the above-mentioned
tag to github, just do the standard git command:

```shell
git push origin release_9_45 # fake numbers, change them to the correct ones.
```

### Publish the release's javadocs.

Run the generate release docs script from the project root setting the
RELEASE_VERSION variable first, e.g.:

```
RELEASE_VERSION=0.29 ./util/generate-latest-docs.sh
```

This will check out the gh-pages branch into a new subfolder, run the
aggregated javadocs (one javadoc for the core plus extensions) and publish
them into a version-specific subfolder. They are then ready to be referenced
by the main documentation site.

### Edit the release info pinned to the git tag in github

Github will automatically create a new "release" (referenceable in the project's
releases section) for each tag that is pushed.  Once you have pushed the tag,
go to https://github.com/google/truth/releases and click the "edit" button.

Add in any changelog information for that release, and clean up the title
which will, by default, simply be the tag name.  Make it all pretty.  If it
is a pre-release/RC check the "pre-release" checkbox. Save your work, and
the release will now be nicely documented.

> ***Note:*** Under the previous release, a link saying *x commits to master
> since this release"* will take you to a handy filtered list of commits which
> can be used as a basis for figuring out the release notes.


## Post-release

### Update version numbers in docs

Create a commit that updates the `_config.yml` file with the new version number. e.g. cl/156885831

### Delete your branch

Once the release is done, and the tag is pushed, the release branch can be
safely deleted.


[GPG]: http://blog.sonatype.com/2010/01/how-to-generate-pgp-signatures-with-maven
[OSSRH Guide]: http://central.sonatype.org/pages/ossrh-guide.html
[settings.xml]: https://books.sonatype.com/nexus-book/reference/_adding_credentials_to_your_maven_settings.html
[OSS]: http://oss.sonatype.org
