# Contributing to Apache Pulsar support for Spring

Apache Pulsar support for Spring is released under the [Apache License 2.0](LICENSE). This document helps you get started if you'd like to contribute or work on the code. See the [README](README.md) for the project background and modules.

If you use AI tooling, see [Using AI tools](#using-ai-tools). Agents also follow [AGENTS.md](AGENTS.md).

## Reporting issues

We use [GitHub issues](https://github.com/apache/pulsar-spring/issues) to track bugs and enhancements.

If you're reporting a bug, please provide as much information as you can, as it speeds up the diagnosis. Ideally, include a small sample project that reproduces the problem.

## Reporting security vulnerabilities

Don't report vulnerabilities in public issues, and don't disclose a vulnerability, or the security nature of a change, in a public issue, pull request or commit. See [SECURITY.md](SECURITY.md) for how to report them.

## Discussing large changes

Please discuss large refactorings on <dev@pulsar.apache.org> before you start working on them. We recommend joining the mailing list before posting to it. The [join instructions](https://pulsar.apache.org/contact/#mailing-lists) are on the Pulsar website.

## Setting up your environment

You need:

- JDK 25. The build uses a Java 25 toolchain and compiles for Java 17.
- Docker, for most of the tests.

Use the included Gradle wrapper, `./gradlew`.

### IntelliJ IDEA

1. Open the root directory of the repository as a Gradle project, and select JDK 25 as the Gradle JVM in **Settings | Build, Execution, Deployment | Build Tools | Gradle**.
2. Install the [Spring Java Format plugin](https://github.com/spring-io/spring-javaformat#intellij-idea). Download `spring-javaformat-intellij-idea-plugin-<version>.jar` from [Maven Central](https://repo1.maven.org/maven2/io/spring/javaformat/spring-javaformat-intellij-idea-plugin/), using the `javaformat` version in [`gradle/build-libs.versions.toml`](gradle/build-libs.versions.toml), and install it with **Settings | Plugins | Install Plugin from Disk**. It activates when IntelliJ IDEA imports the Gradle project, and shows an icon in the status bar. **Code | Reformat Code** then uses the project's formatting.
3. The plugin doesn't configure imports, so set them in **Settings | Editor | Code Style | Java | Imports**:
   - Set **Class count to use import with '\*'** and **Names count to use static import with '\*'** to `999`.
   - Set **Import Layout** to these groups, with a blank line between them:
     1. `import static all other imports`
     2. `java`
     3. `javax`
     4. `org`
     5. `org.springframework`
     6. `all other imports`
4. Keep **Run tests using** set to **Gradle**, which is the default. The build adds the JVM options that the tests need, such as the ones for the Pulsar client and Netty, and the IntelliJ IDEA test runner wouldn't use them.

To also see Checkstyle violations in the editor, set up the CheckStyle-IDEA plugin with the [Spring Java Format instructions](https://github.com/spring-io/spring-javaformat#checkstyle-idea-plugin), and use [`src/checkstyle/checkstyle.xml`](src/checkstyle/checkstyle.xml) as the configuration file.

## Building and testing

```shell
./gradlew build                                # Everything, including the tests
./gradlew build -PsampleTests                  # Also runs the sample application tests
./gradlew build -x test -x integrationTest -x dockerTest   # Everything except the tests
./gradlew :pulsar-spring:test --tests '*PulsarTemplateTests'   # Scope test runs while working
./gradlew :pulsar-spring-boot-autoconfigure:dockerTest
./gradlew :pulsar-spring-integration-tests:integrationTest
./gradlew :pulsar-spring-sample-apps:sample-pulsar-reader:test -PsampleTests   # Scope a sample's tests
./gradlew :pulsar-spring-docs:antora           # Site output in pulsar-spring-docs/build/site
```

The [README](README.md#building) also covers running the tests with another Pulsar image, against published artifacts and in a native image.

## Code conventions

CI enforces the formatting, Checkstyle and license header rules. To check them without running the tests:

```shell
./gradlew checkFormat checkstyleMain checkstyleTest rat
```

- **Formatting:** we use [Spring Java Format](https://github.com/spring-io/spring-javaformat). Apply it with your IDE or with `./gradlew format`. Format checks are disabled for the `pulsar-spring` module, so formatting can also change unrelated files there: review the diff, and keep those changes out of your commit.
- **Packages:** all sources belong under `org.apache.pulsar.spring`.
- **Artifacts:** the group is `org.apache.pulsar`, and published artifact IDs start with `pulsar-spring`. Spring Boot does not manage their versions.
- **Name:** the project is "Apache Pulsar support for Spring". Use "Spring for Apache Pulsar" only when referring to the upstream project it succeeds, as in `NOTICE` and the migration page.
- **Author tags:** don't add `@author` tags. The [ASF board discourages them](https://www.apache.org/foundation/records/minutes/2004/board_minutes_2004_02_18.txt), and the Git history records the authors. Checkstyle and CI reject them.
- **Imports:** Checkstyle requires static imports first, then `java`, `javax`, `org`, `org.springframework` and everything else, as separate groups.
- **Javadoc:** add a Javadoc comment to new classes that says what they're for.
- **Documentation:** there is one site, in `pulsar-spring-docs/src/main/antora`. Don't add documentation to other modules. The Java and Kotlin snippets in `pulsar-spring-docs/src/main` are compiled, and the Configuration Properties appendix is generated from the auto-configuration metadata.
- **Sample apps:** `pulsar-spring-sample-apps` is a separate Gradle build that only shares the version catalog, and uses the artifacts by their coordinates. The main build includes it and substitutes its projects for them. Gradle's `dependencies` and `dependencyInsight` tasks hang for its projects when run through the main build, so run them in that directory with `-PpulsarSpringRepository`, as described in its [`README.adoc`](pulsar-spring-sample-apps/README.adoc).
- **Tests:** add tests for your change. Tests that need a broker use Testcontainers. Clean up anything you start, including containers, so that runs don't leave resources behind.
- **Compatibility:** don't break backward compatibility in public APIs or configuration properties without calling it out, and record user-facing changes in the [What's New page](pulsar-spring-docs/src/main/antora/modules/ROOT/pages/whats-new.adoc).

## Submitting a pull request

- Keep a change focused on its task, and don't bundle unrelated refactoring.
- Rebase your branch on `main` if no one else is using it. Pull requests are squashed or rebased when they're merged, not merged with a merge commit.
- Write commit messages that follow [these conventions](https://tbaggery.com/2008/04/19/a-note-about-git-commit-messages.html), and start the subject with the type of change, such as `fix:`, `docs:`, `build:`, `test:`, `ci:`, `refactor:` or `chore:`.

## Licensing and provenance

- Every new source file needs the ASF license header. Copy the form from a `package-info.java` file, and keep existing notices in imported code. `./gradlew rat` checks this, with exclusions in `.ratignore`.
- Don't introduce code of incompatible or unknown provenance. Never copy verbatim from GPL/AGPL/LGPL, proprietary or unlicensed sources, including Stack Overflow, blog or forum snippets of unclear licensing. Reimplement from specifications or Apache-compatible sources, and follow the [ASF 3rd Party Licensing Policy](https://www.apache.org/legal/resolved.html).

## Using AI tools

Contributions made with AI tools must meet the ASF's [Generative Tooling guidance](https://www.apache.org/legal/generative-tooling.html):

- A human is in the loop and is accountable. Every pull request and every security report must be submitted by a human contributor who has reviewed and verified the change and takes responsibility for it. Fully autonomous agents opening pull requests or filing reports on their own are not acceptable.
- Don't use a tool whose terms restrict the output inconsistently with open source.
- Consider attributing AI assistance. When AI tooling assisted on a change, consider adding an `Assisted-by: <tool/version>` commit trailer, which is the default since a human still does the final review. `Generated-by: <tool/version>` is for minimally-modified generated output.

## Where to ask

See the [discussion channels of the Apache Pulsar community](https://pulsar.apache.org/community/#section-discussions) on the Pulsar website, along with how to join them. The channels include:

- Dev mailing list: <dev@pulsar.apache.org>. We recommend joining it before posting to it.
- Slack: <https://apache-pulsar.slack.com/>

The Apache Pulsar documentation is at <https://pulsar.apache.org>.
