# Releasing Apache Pulsar support for Spring

These are the steps for a manual release, based on the [ASF release policy](https://www.apache.org/legal/release-policy.html). A milestone such as `3.0.0-M1` is a release too, and follows the same steps.

## Prerequisites

- You're a committer. Moving the release to the release directory in [Publish the release](#4-publish-the-release) requires a PMC member.
- You have a GPG key, and its public key is in [KEYS](https://downloads.apache.org/pulsar/KEYS). To add it, commit it to <https://dist.apache.org/repos/dist/release/pulsar/KEYS>, which requires a PMC member.
- You have JDK 25, Docker and Subversion.

Set your ASF ID, unless `APACHE_USER` is already set in your shell:

```shell
export APACHE_USER=<ASF ID>
```

Set the release variables:

```shell
export VERSION=3.0.0-M1
export CANDIDATE=1
export CANDIDATE_URL=https://dist.apache.org/repos/dist/dev/pulsar/pulsar-spring-$VERSION-candidate-$CANDIDATE
```

Set these again if you continue in a new shell.

Some commands copy text to the clipboard with `pbcopy`, which macOS has. On Linux, create an alias for it:

```shell
sudo apt install xsel
alias pbcopy="xsel --clipboard --input"
```

## 1. Create the release candidate

In a clean checkout of `main`, set the version in `gradle.properties`, check the build, commit and tag:

```shell
sed -i.bak "s/^version=.*/version=$VERSION/" gradle.properties && rm gradle.properties.bak
./gradlew build
git commit -am "build: set the version to $VERSION"
git tag -s "v$VERSION-candidate-$CANDIDATE" -m "Apache Pulsar support for Spring $VERSION candidate $CANDIDATE"
```

Create the signed source release in `build/source-release`:

```shell
./gradlew sourceRelease -PuseGpgCmd=true
```

Upload it to the development directory:

```shell
svn import --username "$APACHE_USER" -m "Add Apache Pulsar support for Spring $VERSION candidate $CANDIDATE" \
  build/source-release "$CANDIDATE_URL"
```

Publish the Maven artifacts to a staging repository, with your credentials [as in the README](README.md#publishing):

```shell
 ORG_GRADLE_PROJECT_apacheReleasesUsername="$APACHE_USER" \
 ORG_GRADLE_PROJECT_apacheReleasesPassword=<password> \
 ./gradlew publishArtifacts -PuseGpgCmd=true
```

Log in to <https://repository.apache.org> and click **Staging Repositories** on the left. Select the current repository, which is called something like `orgapachepulsar-XYZ`, and check that it contains the artifacts.

Copy the description for the repository to the clipboard:

```shell
printf "Apache Pulsar support for Spring $VERSION-candidate-$CANDIDATE" | pbcopy
```

Click **Close**, paste the description, and click **Confirm**. Closing takes a few minutes. When it's done, click **Refresh**, which shows a link to the repository, and set its URL:

```shell
export STAGING_REPO=https://repository.apache.org/content/repositories/orgapachepulsar-XYZ
```

Push the commit and the tag:

```shell
git push origin main "v$VERSION-candidate-$CANDIDATE"
```

## 2. Vote

Copy the email to the clipboard, and send it:

```shell
tee >(pbcopy) <<EOF
To: dev@pulsar.apache.org
Subject: [VOTE] Release Apache Pulsar support for Spring $VERSION based on candidate $CANDIDATE

This is a vote to release Apache Pulsar support for Spring $VERSION.

Apache Pulsar support for Spring is derived from Spring for Apache Pulsar, also
known as Spring Pulsar (https://github.com/spring-projects/spring-pulsar), from
the Apache Pulsar starter and auto-configuration in Spring Boot
(https://github.com/spring-projects/spring-boot), and from the Apache Pulsar
binder in Spring Cloud Stream
(https://github.com/spring-cloud/spring-cloud-stream), all licensed under the
Apache License, Version 2.0.
Those works remain the copyright of their original authors and contributors, as
stated in the headers of the source files.

For more information, see the reference documentation and the source repository:
https://pulsar.apache.org/pulsar-spring/reference
https://github.com/apache/pulsar-spring

Source release, with its checksum and signature:
$CANDIDATE_URL

SHA-512 checksum:
$(cat build/source-release/apache-pulsar-spring-$VERSION-src.tar.gz.sha512)

Maven staging repository:
$STAGING_REPO

Git tag:
v$VERSION-candidate-$CANDIDATE (commit $(git rev-parse "v$VERSION-candidate-$CANDIDATE^{commit}"))
https://github.com/apache/pulsar-spring/releases/tag/v$VERSION-candidate-$CANDIDATE

The source release is signed with a key in:
https://downloads.apache.org/pulsar/KEYS

To verify the source release:

    sha512sum -c apache-pulsar-spring-$VERSION-src.tar.gz.sha512
    gpg --verify apache-pulsar-spring-$VERSION-src.tar.gz.asc
    tar xzf apache-pulsar-spring-$VERSION-src.tar.gz
    cd apache-pulsar-spring-$VERSION-src
    ./gradlew assemble

This requires JDK 25, and doesn't run the tests. To also run them, use
./gradlew build instead, which requires Docker and takes about 8-12 minutes.

The vote is open for at least 72 hours, and passes with at least three
binding +1 votes and more binding +1 votes than binding -1 votes. Votes of
PMC members are binding.

[ ] +1 Release this candidate
[ ] -1 Don't release this candidate, because ...
EOF
```

If the candidate has to be replaced:

1. Reply to the thread that the vote is cancelled.
2. **Drop** the staging repository, and remove the upload with `svn rm -m "Remove Apache Pulsar support for Spring $VERSION candidate $CANDIDATE" "$CANDIDATE_URL"`.
3. Fix the problem on `main`, increase `$CANDIDATE` and repeat from step 1.

## 3. Close the vote

When the vote has passed, copy this reply to the clipboard, fill in the votes, and send it on the vote thread:

```shell
tee >(pbcopy) <<EOF
The vote to release Apache Pulsar support for Spring $VERSION based on candidate $CANDIDATE is now closed.

The vote PASSED with X binding +1 votes, Y non-binding +1 votes and 0 -1 votes.

Binding +1 votes:

  - <name>

Non-binding +1 votes:

  - <name>

The release announcement will follow shortly.
EOF
```

## 4. Publish the release

The published files must be the exact ones that were voted on, so don't rebuild them.

Move the source release to the release directory. A PMC member must do this:

```shell
svn mv --username "$APACHE_USER" -m "Release Apache Pulsar support for Spring $VERSION" \
  "$CANDIDATE_URL" "https://dist.apache.org/repos/dist/release/pulsar/pulsar-spring-$VERSION"
```

In <https://repository.apache.org>, select the staging repository from the vote email, and check that its description is the candidate that passed. Then **Release** it, which publishes the artifacts to Maven Central.

Tag the release:

```shell
git tag -s "v$VERSION" "v$VERSION-candidate-$CANDIDATE^{commit}" -m "Apache Pulsar support for Spring $VERSION"
git push origin "v$VERSION"
```

Set the next `-SNAPSHOT` version in `gradle.properties`, and commit and push it to `main`:

```shell
export NEXT_VERSION=<next version>-SNAPSHOT
sed -i.bak "s/^version=.*/version=$NEXT_VERSION/" gradle.properties && rm gradle.properties.bak
git commit -am "build: set the version to $NEXT_VERSION"
git push origin main
```

If the new release supersedes an earlier one in the same line, remove the earlier one from <https://dist.apache.org/repos/dist/release/pulsar/>. It stays available at <https://archive.apache.org/dist/pulsar/>.

## 5. Update the documentation

Build the reference documentation and the Javadoc from the release tag. Build them from this clone, not from the source archive, as Antora needs a Git repository.

```shell
export PULSAR_SPRING_DIR="$PWD"
git checkout "v$VERSION"
./gradlew :pulsar-spring-docs:antora :pulsar-spring-docs:aggregatedJavadoc
git checkout main
```

Set the location of your clone of [apache/pulsar-site](https://github.com/apache/pulsar-site):

```shell
export PULSAR_SITE_DIR=<path to pulsar-site>
```

If you don't have a clone yet, create one:

```shell
git clone https://github.com/apache/pulsar-site.git "$PULSAR_SITE_DIR"
```

Replace the documentation in it on an up-to-date `main` branch, and commit the change:

```shell
cd "$PULSAR_SITE_DIR"
git checkout main
git pull --ff-only origin main
rm -rf static/pulsar-spring/reference static/pulsar-spring/api
cp -R "$PULSAR_SPRING_DIR/pulsar-spring-docs/build/site" static/pulsar-spring/reference
cp -R "$PULSAR_SPRING_DIR/pulsar-spring-docs/build/api" static/pulsar-spring/api
git add -A static/pulsar-spring
git commit -m "docs: update the Apache Pulsar support for Spring documentation to $VERSION"
cd "$PULSAR_SPRING_DIR"
```

Push the commit:

```shell
git -C "$PULSAR_SITE_DIR" push origin main
```

## 6. Update the download page

Wait at least one hour after moving the source release, so that it reaches the distribution servers.

Add the release to `src/pages/download.mdx` in [apache/pulsar-site](https://github.com/apache/pulsar-site). The link to the archive must use the closer.lua script. The checksum and signature links use `downloads.apache.org`. Copy the links to the clipboard:

```shell
tee >(pbcopy) <<EOF
https://www.apache.org/dyn/closer.lua/pulsar/pulsar-spring-$VERSION/apache-pulsar-spring-$VERSION-src.tar.gz?action=download
https://downloads.apache.org/pulsar/pulsar-spring-$VERSION/apache-pulsar-spring-$VERSION-src.tar.gz.sha512
https://downloads.apache.org/pulsar/pulsar-spring-$VERSION/apache-pulsar-spring-$VERSION-src.tar.gz.asc
EOF
```

## 7. Announce the release

Once the download page is live and the artifacts are on Maven Central, copy the email to the clipboard, and send it from your `apache.org` address:

```shell
tee >(pbcopy) <<EOF
To: dev@pulsar.apache.org, users@pulsar.apache.org, announce@apache.org
Subject: [ANNOUNCE] Apache Pulsar support for Spring $VERSION released

The Apache Pulsar team is pleased to announce the release of Apache Pulsar
support for Spring $VERSION.

Apache Pulsar support for Spring makes it easier to build Apache Pulsar
applications with Spring. It provides PulsarTemplate for publishing messages,
@PulsarListener and @PulsarReader for consuming them, a Spring Boot starter with
auto-configuration and a Spring Cloud Stream binder.

Apache Pulsar support for Spring is derived from Spring for Apache Pulsar, also
known as Spring Pulsar (https://github.com/spring-projects/spring-pulsar), from
the Apache Pulsar starter and auto-configuration in Spring Boot
(https://github.com/spring-projects/spring-boot), and from the Apache Pulsar
binder in Spring Cloud Stream
(https://github.com/spring-cloud/spring-cloud-stream), all licensed under the
Apache License, Version 2.0.
Those works remain the copyright of their original authors and contributors, as
stated in the headers of the source files.

Download:
https://pulsar.apache.org/download/

Documentation:
https://pulsar.apache.org/pulsar-spring/reference

Source repository:
https://github.com/apache/pulsar-spring

The artifacts are available on Maven Central with the org.apache.pulsar group ID.
EOF
```
