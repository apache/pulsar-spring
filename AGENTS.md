# Agent guide for Apache Pulsar support for Spring

Guidance for AI coding assistants (Claude Code, Copilot, Cursor, Gemini, Codex, Aider, and similar
tools) working in this repository.

This project integrates Apache Pulsar with Spring Framework, Spring Boot and Spring Cloud Stream.
Prioritize **correctness, thread safety, maintainability, and backward compatibility**.

## Read first

- **[`CONTRIBUTING.md`](CONTRIBUTING.md)** has the build and test commands, the code conventions, the
  pull request guidelines, and the licensing and AI tooling rules. They all apply to you, so read it
  before changing anything.
- [`README.md`](README.md) has the project background, the modules, and how to publish and release.

This file only adds what is specific to agents.

## Agent guardrails

- **The human is accountable.** Every pull request and every security report must be submitted by a human
  contributor who has reviewed and verified the change. Don't open pull requests or file reports on your
  own: you assist, the human is accountable.
- **Verify, don't fabricate.** Before you use a class, method, field, configuration key, Gradle task,
  plugin, or DSL name, confirm it actually exists (search the code or the build files) — don't invent
  APIs, test assertions, or symbol names.
- **Don't reproduce code of incompatible or unknown provenance,** as described in
  [Licensing and provenance](CONTRIBUTING.md#licensing-and-provenance).
- **Confirm state-changing actions.** Get the user's explicit confirmation before pushing, opening or
  updating a PR, or posting a comment. Being asked to start a task is not standing authorization for
  these outward-facing actions.
- **Don't publish artifacts** to `repository.apache.org` unless the user explicitly asks. To test
  publishing, use `file://` URLs as described in [`README.md`](README.md#publishing).
- **Never disclose a vulnerability** — or the security nature of a change — in a public issue, PR, or
  commit. Follow [`SECURITY.md`](SECURITY.md).
- **Check before claiming conformance.** Say which checks you ran, and don't describe a change as tested
  if you only compiled it.
- **Stay in scope.** Keep a change focused on its task, and don't bundle unrelated refactoring.
- **Consider attributing your assistance** with a commit trailer, as described in
  [Using AI tools](CONTRIBUTING.md#using-ai-tools).
