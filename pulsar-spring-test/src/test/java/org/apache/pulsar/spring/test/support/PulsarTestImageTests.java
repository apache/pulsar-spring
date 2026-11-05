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

package org.apache.pulsar.spring.test.support;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.pulsar.PulsarVersion;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;
import org.testcontainers.pulsar.PulsarContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Tests for {@link PulsarTestImage}.
 */
class PulsarTestImageTests {

	@AfterEach
	void clearImageSystemProperty() {
		System.clearProperty(PulsarTestImage.IMAGE_SYSTEM_PROPERTY);
	}

	@Test
	@DisabledIfEnvironmentVariable(named = PulsarTestImage.IMAGE_ENVIRONMENT_VARIABLE, matches = ".*\\S.*")
	void defaultImageVersionMatchesPulsarClientVersion() {
		assertThat(PulsarTestImage.getDefaultImage().asCanonicalNameString())
			.isEqualTo("apachepulsar/pulsar:" + PulsarVersion.getVersion());
		assertThat(PulsarTestContainerSupport.getPulsarImage()).isEqualTo(PulsarTestImage.getDefaultImage());
	}

	@Test
	void systemPropertySetsDefaultImage() {
		System.setProperty(PulsarTestImage.IMAGE_SYSTEM_PROPERTY, "registry.example.com/pulsar:1.2.3");
		assertThat(PulsarTestImage.getDefaultImage().asCanonicalNameString())
			.isEqualTo("registry.example.com/pulsar:1.2.3");
		assertImage(PulsarTestImage.createContainer(), "registry.example.com/pulsar:1.2.3");
		assertThat(PulsarTestContainerSupport.getPulsarImage().asCanonicalNameString())
			.isEqualTo("registry.example.com/pulsar:1.2.3");
	}

	@Test
	@DisabledIfEnvironmentVariable(named = PulsarTestImage.IMAGE_ENVIRONMENT_VARIABLE, matches = ".*\\S.*")
	void blankSystemPropertyIsIgnored() {
		System.setProperty(PulsarTestImage.IMAGE_SYSTEM_PROPERTY, " ");
		assertThat(PulsarTestImage.getDefaultImage().asCanonicalNameString())
			.isEqualTo("apachepulsar/pulsar:" + PulsarVersion.getVersion());
	}

	@Test
	void createContainerWithTagIgnoresConfiguredImage() {
		System.setProperty(PulsarTestImage.IMAGE_SYSTEM_PROPERTY, "registry.example.com/pulsar:1.2.3");
		assertImage(PulsarTestImage.createContainer("4.5.6"), "apachepulsar/pulsar:4.5.6");
	}

	@Test
	void createContainerUsesDefaultImage() {
		assertImage(PulsarTestImage.createContainer(), PulsarTestImage.getDefaultImage().asCanonicalNameString());
	}

	@Test
	void createContainerWithTagUsesTag() {
		assertImage(PulsarTestImage.createContainer("1.2.3"), "apachepulsar/pulsar:1.2.3");
	}

	@Test
	void createContainerWithImageUsesImage() {
		DockerImageName image = DockerImageName.parse("registry.example.com/pulsar:1.2.3");
		assertImage(PulsarTestImage.createContainer(image), "registry.example.com/pulsar:1.2.3");
	}

	@Test
	void createContainerAdvertisesLocalhost() {
		assertThat(PulsarTestImage.createContainer().getEnvMap()).containsEntry("PULSAR_PREFIX_advertisedAddress",
				"localhost");
	}

	private static void assertImage(PulsarContainer container, String expected) {
		assertThat(container.getImage().toString()).contains("imageName=" + expected + ",");
	}

}
