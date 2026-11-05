/*
 * Copyright 2016-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.apache.pulsar.spring.gradle.convention

import org.gradle.api.Plugin
import org.gradle.api.Project

import org.apache.pulsar.spring.gradle.ProjectUtils

class RepositoryConventionPlugin implements Plugin<Project> {

	@Override
	void apply(Project project) {
		String[] forceMavenRepositories = ((String) project.findProperty("forceMavenRepositories"))?.split(',')
		boolean isSnapshot = forceMavenRepositories == null ? ProjectUtils.isSnapshot(project)
				: forceMavenRepositories.contains('snapshot')

		project.repositories {
			if (forceMavenRepositories?.contains('local')) {
				mavenLocal()
			}
			mavenCentral()
			if (isSnapshot) {
				maven {
					name = 'apache-snapshot'
					url = 'https://repository.apache.org/snapshots/'
					mavenContent {
						snapshotsOnly()
					}
				}
			}
		}
	}

}
