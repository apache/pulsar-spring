/*
 * Copyright 2012-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.pulsar.spring.gradle;

import java.util.Collections;
import java.util.Set;

import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.plugins.JavaBasePlugin;
import org.gradle.api.plugins.JavaPlugin;
import org.gradle.api.plugins.quality.Checkstyle;
import org.gradle.api.plugins.quality.CheckstyleExtension;
import org.gradle.api.plugins.quality.CheckstylePlugin;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.javadoc.Javadoc;
import org.gradle.api.tasks.testing.Test;
import org.gradle.api.tasks.testing.logging.TestExceptionFormat;
import org.gradle.api.tasks.testing.logging.TestLogEvent;
import org.gradle.external.javadoc.CoreJavadocOptions;

import org.apache.pulsar.spring.gradle.optional.OptionalDependenciesPlugin;
import org.apache.pulsar.spring.gradle.testing.TestFailuresPlugin;

import io.spring.javaformat.gradle.SpringJavaFormatPlugin;
import io.spring.javaformat.gradle.tasks.CheckFormat;
import io.spring.javaformat.gradle.tasks.Format;

/**
 * Conventions that are applied in the presence of the {@link JavaBasePlugin}. When the
 * plugin is applied:
 *
 * <ul>
 * <li>The project is configured with source and target compatibility of 17
 * <li>{@link SpringJavaFormatPlugin Spring Java Format}, {@link CheckstylePlugin
 * Checkstyle} and {@link TestFailuresPlugin Test Failures}.
 * <li>{@link Test} tasks are configured:
 * <ul>
 * <li>to use JUnit Platform
 * <li>with a max heap of 1024M
 * <li>with Mockito as a Java agent when it is on the test classpath
 * <li>with the JVM options that the Pulsar client and Netty need
 * <li>to run after any Checkstyle and format checking tasks
 * </ul>
 * <li>A {@code testRuntimeOnly} dependency upon
 * {@code org.junit.platform:junit-platform-launcher} is added to projects with the
 * {@link JavaPlugin} applied
 * <li>{@link JavaCompile}, {@link Javadoc}, and {@link Format} tasks are configured to
 * use UTF-8 encoding
 * <li>{@link JavaCompile} tasks are configured to:
 * <ul>
 * <li>Use {@code -parameters}.
 * <li>Treat warnings as errors
 * <li>Enable {@code unchecked}, {@code deprecation}, {@code rawtypes}, and {@code varags}
 * warnings
 * </ul>
 * <li>{@code pulsar-spring-dependencies} is used for dependency management</li>
 * </ul>
 *
 * <p/>
 *
 * @author Andy Wilkinson
 * @author Christoph Dreis
 * @author Mike Smithson
 * @author Scott Frederick
 * @author Chris Bono
 */
public class JavaConventionsPlugin implements Plugin<Project> {

	@Override
	public void apply(Project project) {
		project.getPlugins().withType(JavaBasePlugin.class, (java) -> {
			configureSpringJavaFormat(project);
			configureJavadocConventions(project);
			configureTestConventions(project);
			configureDependencyManagement(project);
		});
	}

	private void configureSpringJavaFormat(Project project) {
		project.getPlugins().apply(SpringJavaFormatPlugin.class);
		project.getTasks().withType(Format.class, (format) -> {
			format.setEncoding("UTF-8");
			project.getTasks().named("compileJava", JavaCompile.class, format::mustRunAfter);
		});
		project.getPlugins().apply(CheckstylePlugin.class);
		CheckstyleExtension checkstyle = project.getExtensions().getByType(CheckstyleExtension.class);
		checkstyle.setToolVersion("10.25.0");
		checkstyle.getConfigDirectory().set(project.getRootProject().file("src/checkstyle"));
		String version = SpringJavaFormatPlugin.class.getPackage().getImplementationVersion();
		DependencySet checkstyleDependencies = project.getConfigurations().getByName("checkstyle").getDependencies();
		checkstyleDependencies
				.add(project.getDependencies().create("com.puppycrawl.tools:checkstyle:" + checkstyle.getToolVersion()));
		checkstyleDependencies
				.add(project.getDependencies().create("io.spring.javaformat:spring-javaformat-checkstyle:" + version));
	}

	private void configureJavadocConventions(Project project) {
		project.getTasks().withType(Javadoc.class, (javadoc) -> {
			CoreJavadocOptions options = (CoreJavadocOptions) javadoc.getOptions();
			options.source("25");
			options.encoding("UTF-8");
			options.addStringOption("Xdoclint:none", "-quiet");
		});
	}

	private void configureTestConventions(Project project) {
		project.getPlugins().apply(TestFailuresPlugin.class);
		project.getTasks().withType(Test.class, (test) -> {
			test.useJUnitPlatform();
			test.setMaxHeapSize("1024M");
			test.testLogging(testLoggingContainer -> {
				testLoggingContainer.setEvents(Set.of(TestLogEvent.SKIPPED, TestLogEvent.FAILED));
				testLoggingContainer.setShowStandardStreams(project.hasProperty("showStandardStreams"));
				testLoggingContainer.setShowExceptions(true);
				testLoggingContainer.setShowStackTraces(true);
				testLoggingContainer.setExceptionFormat(TestExceptionFormat.FULL);
			});
			test.jvmArgs(
					"--add-opens", "java.base/java.nio=ALL-UNNAMED",
					"--add-opens", "java.base/jdk.internal.misc=ALL-UNNAMED",
					"--add-opens", "java.base/java.util.zip=ALL-UNNAMED"
					);
			// Used by Netty together with the java.nio and jdk.internal.misc options above
			test.systemProperty("io.netty.tryReflectionSetAccessible", "true");
			test.doFirst(new AddTestJvmArguments());
			// PulsarTestImage reads this, so results for another image must not be reused
			test.getInputs()
				.property("pulsarTestImage",
						project.getProviders().environmentVariable("PULSAR_TEST_IMAGE").orElse(""));
			project.getTasks().withType(Checkstyle.class, test::mustRunAfter);
			project.getTasks().withType(CheckFormat.class, test::mustRunAfter);
		});
		project.getPlugins().withType(JavaPlugin.class, (javaPlugin) -> project.getDependencies()
				.add(JavaPlugin.TEST_RUNTIME_ONLY_CONFIGURATION_NAME, "org.junit.platform:junit-platform-launcher"));
	}

	private void configureDependencyManagement(Project project) {
		ConfigurationContainer configurations = project.getConfigurations();
		Configuration dependencyManagement = configurations.create("dependencyManagement", (configuration) -> {
			configuration.setCanBeConsumed(false);
			configuration.setCanBeResolved(false);
		});
		configurations
				.matching((c) -> c.getName().endsWith("Classpath") || c.getName().toLowerCase().endsWith("annotationprocessor"))
				.all((c) -> c.extendsFrom(dependencyManagement));
		Dependency pulsarDependencies = project.getDependencies().enforcedPlatform(project.getDependencies()
				.project(Collections.singletonMap("path", ":pulsar-spring-dependencies")));
		dependencyManagement.getDependencies().add(pulsarDependencies);
		project.getPlugins().withType(OptionalDependenciesPlugin.class, (optionalDependencies) -> configurations
				.getByName(OptionalDependenciesPlugin.OPTIONAL_CONFIGURATION_NAME).extendsFrom(dependencyManagement));
	}

	/**
	 * Adds the JVM arguments that depend on the test classpath and the Java version. They
	 * are added when the task runs since both are task inputs already, and this keeps the
	 * absolute path of the Mockito jar out of the task's inputs.
	 */
	private static final class AddTestJvmArguments implements Action<Task> {

		@Override
		public void execute(Task task) {
			Test test = (Test) task;
			// Mockito can't rely on attaching itself in future JDK releases
			test.getClasspath().getFiles().stream()
					.filter((file) -> file.getName().matches("mockito-core-.*\\.jar"))
					.forEach((file) -> test.jvmArgs("-javaagent:" + file.getAbsolutePath()));
			// These options don't exist or aren't needed on earlier Java versions
			if (test.getJavaVersion().isCompatibleWith(JavaVersion.VERSION_23)) {
				// The Netty of the Pulsar client uses Unsafe
				test.jvmArgs("--sun-misc-unsafe-memory-access=allow");
			}
			if (test.getJavaVersion().isCompatibleWith(JavaVersion.VERSION_24)) {
				// The Netty of the Pulsar client loads native libraries
				test.jvmArgs("--enable-native-access=ALL-UNNAMED");
			}
		}

	}

}
