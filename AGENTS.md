# Agent guide for Apache Pulsar support for Spring

Guidance for AI coding assistants (Claude Code, Copilot, Cursor, Gemini, Codex, Aider, and similar
tools) working in this repository. Read [`README.md`](README.md) for the project background and modules.

This project integrates Apache Pulsar with Spring Framework, Spring Boot and Spring Cloud Stream.
Prioritize **correctness, thread safety, maintainability, and backward compatibility**.

## Licensing and provenance (read first)

This project is licensed under the Apache License 2.0, and all contributions must meet the ASF's
[Generative Tooling guidance](https://www.apache.org/legal/generative-tooling.html):

- **A human is in the loop and is accountable.** Every pull request and every security report must be
  submitted by a human contributor who has reviewed and verified the change and takes responsibility
  for it. Fully autonomous agents opening PRs or filing reports on their own are not acceptable — the
  AI assists, the human is accountable.
- **Don't introduce code of incompatible or unknown provenance.** Never copy verbatim from
  GPL/AGPL/LGPL, proprietary, or unlicensed sources (including Stack Overflow / blog / forum snippets of
  unclear licensing), and don't use a tool whose terms restrict the output inconsistently with open
  source. Reimplement from specifications or Apache-compatible sources, and follow the
  [ASF 3rd Party Licensing Policy](https://www.apache.org/legal/resolved.html).
- **Every new source file needs the ASF license header.** Copy the form from a `package-info.java` file,
  and keep existing notices in imported code. `./gradlew rat` checks this, with exclusions in `.ratignore`.
- **Don't commit modified copies of MPL-licensed files,** such as those of the Spring Antora UI bundle.
  It is downloaded during the build, and is customized in `pulsar-spring-docs/src/main/antora/ui` with
  this project's own files.
- **Consider attributing AI assistance.** When AI tooling assisted on a change, consider adding an
  `Assisted-by: <tool/version>` commit trailer (the default, since a human still does the final
  review); `Generated-by: <tool/version>` is for minimally-modified generated output.

## Agent guardrails

- **Verify, don't fabricate.** Before you use a class, method, field, configuration key, Gradle task,
  plugin, or DSL name, confirm it actually exists (search the code or the build files) — don't invent
  APIs, test assertions, or symbol names.
- **Confirm state-changing actions.** Get the user's explicit confirmation before pushing, opening or
  updating a PR, or posting a comment. Being asked to start a task is not standing authorization for
  these outward-facing actions.
- **Don't publish artifacts** to `repository.apache.org` unless the user explicitly asks. To test
  publishing, use `file://` URLs as described in [`README.md`](README.md#publishing).
- **Check before claiming conformance.** Say which checks you ran, and don't describe a change as tested
  if you only compiled it.
- **Stay in scope.** Keep a change focused on its task, and don't bundle unrelated refactoring.

## Building and testing

The build uses the Gradle wrapper and a Java 25 toolchain, and compiles for Java 17. Docker is required
for most tests.

```shell
./gradlew build                                # Everything, including tests (about 20 minutes)
./gradlew build -PsampleTests                  # Also runs the sample application tests
./gradlew :pulsar-spring-sample-apps:sample-pulsar-reader:test -PsampleTests   # Scope a sample's tests
./gradlew build -x test -x integrationTest -x dockerTest   # Everything except the tests
./gradlew :pulsar-spring:test --tests '*PulsarTemplateTests'   # Scope test runs while working
./gradlew :pulsar-spring-boot-autoconfigure:dockerTest
./gradlew :pulsar-spring-integration-tests:integrationTest
./gradlew :pulsar-spring-docs:antora           # Site output in pulsar-spring-docs/build/site
```

`./gradlew format` applies Spring Java Format. Format checks are disabled for the `pulsar-spring` module,
so it can also reformat unrelated files there: review the diff and keep those changes out of your commit.

## Project conventions

- **Packages:** all sources belong under `org.apache.pulsar.spring`.
- **Artifacts:** the group is `org.apache.pulsar`, and published artifact IDs start with `pulsar-spring`.
  Spring Boot does not manage their versions.
- **Name:** the project is "Apache Pulsar support for Spring". Use "Spring for Apache Pulsar" only when
  referring to the upstream project it succeeds, as in `NOTICE` and the migration page.
- **Imports:** Checkstyle requires static imports first, then `java`, `javax`, `org`, `org.springframework`
  and everything else, as separate groups.
- **Documentation:** there is one site, in `pulsar-spring-docs/src/main/antora`. Don't add documentation
  to other modules. The Java and Kotlin snippets in `pulsar-spring-docs/src/main` are compiled, and the
  Configuration Properties appendix is generated from the auto-configuration metadata.
- **Sample apps:** `pulsar-spring-sample-apps` is a separate Gradle build that only shares the version
  catalog, and uses the artifacts by their coordinates. The main build includes it and substitutes its
  projects for them. Gradle's `dependencies` and `dependencyInsight` tasks hang for its projects when run
  through the main build, so run them in that directory with `-PpulsarSpringRepository`, as described in
  its [`README.adoc`](pulsar-spring-sample-apps/README.adoc).
- **Tests:** tests that need a broker use Testcontainers. Clean up anything you start, including
  containers, so that runs don't leave resources behind.

## Critical rules

1. **Don't break backward compatibility** in public APIs or configuration properties without calling it
   out, and record user-facing changes in the What's New page.
2. **Security:** never disclose a vulnerability — or the security nature of a change — in a public issue,
   PR, or commit. Follow [`SECURITY.md`](SECURITY.md).
3. **Large changes:** discuss large refactorings on `dev@pulsar.apache.org` first.

## Where to ask

- Dev mailing list: <dev@pulsar.apache.org> · Slack: <https://apache-pulsar.slack.com/>
- Issues: <https://github.com/apache/pulsar-spring/issues>
- Apache Pulsar documentation: <https://pulsar.apache.org>
