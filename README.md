# Apache Pulsar support for Spring

Apache Pulsar support for Spring makes it easier to build [Apache Pulsar](https://pulsar.apache.org/) applications with Spring. It provides `PulsarTemplate` for publishing messages, `@PulsarListener` and `@PulsarReader` for consuming them, Spring Boot auto-configuration and a Spring Cloud Stream binder.

## Documentation

The reference documentation will be available at <https://pulsar.apache.org/pulsar-spring/reference>.

Until it's published, you can build it locally with the command in [Building](#building).

## Background

This project is the successor to Spring for Apache Pulsar. It brings together code that was previously maintained in three repositories:

| Previous repository | Code |
|---|---|
| [spring-projects/spring-pulsar](https://github.com/spring-projects/spring-pulsar) | Core library, cache providers and test utilities |
| [spring-projects/spring-boot](https://github.com/spring-projects/spring-boot) | Spring Boot auto-configuration and starters |
| [spring-cloud/spring-cloud-stream](https://github.com/spring-cloud/spring-cloud-stream) | Spring Cloud Stream binder |

The version has been bumped to `3.0.0-M1` to indicate that it's the successor to Spring for Apache Pulsar `2.0.x`. Further development happens under the Apache Pulsar project in this repository.

The Maven coordinates and Java packages have changed. See [Migration from Spring's Pulsar support](https://pulsar.apache.org/pulsar-spring/reference/migration.html) for the changes to make in your application.

## Modules

All artifacts use the `org.apache.pulsar` group ID.

| Artifact | Description |
|---|---|
| `pulsar-spring` | Core library |
| `pulsar-spring-boot-autoconfigure` | Spring Boot auto-configuration |
| `pulsar-spring-boot-starter` | Starter for Spring Boot applications |
| `pulsar-spring-boot-starter-test` | Starter for testing Spring Boot applications |
| `pulsar-spring-cloud-stream-binder` | Spring Cloud Stream binder |
| `pulsar-spring-cache-provider` | Cache provider API |
| `pulsar-spring-cache-provider-caffeine` | Caffeine cache provider |
| `pulsar-spring-test` | Test utilities |
| `pulsar-spring-bom` | Bill of materials |

Sample applications are in [pulsar-spring-sample-apps](pulsar-spring-sample-apps/README.adoc).

## Building

The build uses the included Gradle wrapper and a Java 25 toolchain. Docker is required for the tests.

```shell
# Full build, including tests
./gradlew build

# Also run the sample application tests
./gradlew build -PsampleTests

# Run the sample application tests against published artifacts in a local repository
./gradlew publishAllPublicationsToLocalRepository
(cd pulsar-spring-sample-apps && ../gradlew runAllSampleTests -PsampleTests -PpulsarSpringRepository=../build/publications/repos)

# Reference documentation, generated in pulsar-spring-docs/build/site
./gradlew :pulsar-spring-docs:antora
```

## Publishing

Artifacts are published to the [Apache repository](https://repository.apache.org/). `publishArtifacts` uses the snapshot repository for `-SNAPSHOT` versions and a staging repository for other versions, which must be signed. It refuses to publish a version to the wrong repository.

Pass your ASF credentials as environment variables so that they aren't stored in `gradle.properties`, and start the command with a space to keep the password out of your shell history:

```shell
# Snapshot
 ORG_GRADLE_PROJECT_apacheSnapshotsUsername=<ASF ID> \
 ORG_GRADLE_PROJECT_apacheSnapshotsPassword=<password> \
 ./gradlew publishArtifacts

# Release or milestone, with -Psigning.gnupg.keyName=<key ID> to choose a key other than the default
 ORG_GRADLE_PROJECT_apacheReleasesUsername=<ASF ID> \
 ORG_GRADLE_PROJECT_apacheReleasesPassword=<password> \
 ./gradlew publishArtifacts -PuseGpgCmd=true
```

Set `-PapacheSnapshotsRepoUrl` and `-PapacheReleasesRepoUrl` to `file://` URLs to test the publication without uploading anything.

## Source release

`sourceRelease` creates the source release for an ASF release vote in `build/source-release`: an archive of the committed tree, with a SHA-512 checksum and a detached signature.

```shell
./gradlew sourceRelease -PuseGpgCmd=true
```

For a non-snapshot version, it fails if there are uncommitted changes, if the committed `gradle.properties` has a different version, or if signing isn't configured. The reference documentation is skipped when building from the archive because Antora needs a git repository, so build the site from a clone.

## Security

Don't report vulnerabilities in public issues. See [SECURITY.md](SECURITY.md) for how to report them.

## License

Licensed under the [Apache License, Version 2.0](LICENSE).
