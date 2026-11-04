/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under this License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pulsar.spring.gradle.publish;

import java.net.URI;

import org.gradle.api.GradleException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.artifacts.repositories.PasswordCredentials;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin;
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;

import org.apache.pulsar.spring.gradle.ProjectUtils;

/**
 * Adds the Apache Nexus repositories at https://repository.apache.org/ as publishing
 * targets, following the conventions of apache/pulsar:
 * <ul>
 * <li>{@code apacheSnapshots} is for {@code -SNAPSHOT} versions and
 * {@code apacheReleases} is for all other versions, which go to a staging repository.
 * Publishing a version to the wrong repository fails before anything is uploaded.
 * <li>Credentials come from the {@code apacheSnapshotsUsername},
 * {@code apacheSnapshotsPassword}, {@code apacheReleasesUsername} and
 * {@code apacheReleasesPassword} Gradle properties, which are resolved when publishing.
 * <li>The {@code apacheSnapshotsRepoUrl} and {@code apacheReleasesRepoUrl} Gradle
 * properties override the URLs, for example with a {@code file://} URL for testing.
 * <li>Uploads run one at a time so that a release ends up in a single staging repository,
 * since Nexus creates one implicitly on the first upload.
 * </ul>
 */
public class ApacheRepositoriesPlugin implements Plugin<Project> {

	static final String SNAPSHOTS_REPOSITORY = "apacheSnapshots";

	static final String RELEASES_REPOSITORY = "apacheReleases";

	private static final String SNAPSHOTS_URL = "https://repository.apache.org/content/repositories/snapshots";

	private static final String RELEASES_URL = "https://repository.apache.org/service/local/staging/deploy/maven2";

	@Override
	public void apply(Project project) {
		project.getPlugins().withType(MavenPublishPlugin.class).all((mavenPublish) -> {
			PublishingExtension publishing = project.getExtensions().getByType(PublishingExtension.class);
			publishing.getRepositories().maven((maven) -> configure(project, maven, SNAPSHOTS_REPOSITORY, SNAPSHOTS_URL));
			publishing.getRepositories().maven((maven) -> configure(project, maven, RELEASES_REPOSITORY, RELEASES_URL));
		});
		Provider<MavenPublishLock> lock = project.getGradle().getSharedServices()
				.registerIfAbsent("mavenPublishLock", MavenPublishLock.class, (spec) -> spec.getMaxParallelUsages().set(1));
		boolean snapshot = ProjectUtils.isSnapshot(project);
		String version = String.valueOf(project.getVersion());
		boolean signed = SpringSigningPlugin.isSigningConfigured(project);
		project.getTasks().withType(PublishToMavenRepository.class).configureEach((task) -> {
			task.usesService(lock);
			boolean toSnapshots = task.getName().endsWith("ToApacheSnapshotsRepository");
			boolean toReleases = task.getName().endsWith("ToApacheReleasesRepository");
			task.doFirst((action) -> {
				if (toSnapshots && !snapshot) {
					throw new GradleException("Refusing to publish non-snapshot version '" + version + "' to the '"
							+ SNAPSHOTS_REPOSITORY + "' repository");
				}
				if (toReleases && snapshot) {
					throw new GradleException("Refusing to publish snapshot version '" + version + "' to the '"
							+ RELEASES_REPOSITORY + "' repository");
				}
				if (toReleases && !signed) {
					throw new GradleException("Refusing to publish version '" + version + "' without signing. "
							+ "Set -PuseGpgCmd=true, signing.keyId or signingKey");
				}
			});
		});
	}

	private void configure(Project project, MavenArtifactRepository maven, String name, String defaultUrl) {
		maven.setName(name);
		URI url = project.uri(project.getProviders().gradleProperty(name + "RepoUrl").getOrElse(defaultUrl));
		maven.setUrl(url);
		// The file transport rejects credentials
		if (!"file".equals(url.getScheme())) {
			maven.credentials(PasswordCredentials.class);
		}
	}

	/**
	 * Limits uploads to one at a time across the build.
	 */
	public abstract static class MavenPublishLock implements BuildService<BuildServiceParameters.None> {

	}

}
