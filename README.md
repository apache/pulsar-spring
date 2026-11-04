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

The Maven coordinates and Java packages have changed. See [What's New in 3.0](https://pulsar.apache.org/pulsar-spring/reference/whats-new.html#what-s-new-in-3-0-since-2-0) for the migration information.

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

# Reference documentation, generated in pulsar-spring-docs/build/site
./gradlew :pulsar-spring-docs:antora
```

## License

Licensed under the [Apache License, Version 2.0](LICENSE).
