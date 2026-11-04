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

package org.apache.pulsar.spring.gradle.check;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.sonarqube.gradle.SonarQubeExtension;
import org.sonarqube.gradle.SonarQubePlugin;

import org.apache.pulsar.spring.gradle.ProjectLinks;

/**
 * Adds a version of SonarQube to use and configures it.
 *
 * @author Chris Bono
 */
public class SonarQubeConventionsPlugin implements Plugin<Project> {

	@Override
	public void apply(final Project project) {
		project.getPluginManager().apply(SonarQubePlugin.class);
		project.getExtensions().configure(SonarQubeExtension.class, (sonarExtension) ->
				sonarExtension.properties((sonarProperties) -> {
					sonarProperties.property("sonar.projectName", project.getName());
					sonarProperties.property("sonar.jacoco.reportPath", project.getBuildDir().getName() + "/jacoco.exec");
					sonarProperties.property("sonar.links.homepage", ProjectLinks.HOMEPAGE.link());
					sonarProperties.property("sonar.links.ci", ProjectLinks.CI.link());
					sonarProperties.property("sonar.links.issue", ProjectLinks.ISSUES.link());
					sonarProperties.property("sonar.links.scm", ProjectLinks.SCM_CONNECTION.link());
					sonarProperties.property("sonar.links.scm_dev", ProjectLinks.SCM_DEV_CONNECTION.link());
				}));
	}
}
