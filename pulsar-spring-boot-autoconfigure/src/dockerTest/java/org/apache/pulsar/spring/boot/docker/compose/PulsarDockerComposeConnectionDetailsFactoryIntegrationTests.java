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

/*
 * Copyright 2012-present the original author or authors.
 */

package org.apache.pulsar.spring.boot.docker.compose;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Set;

import org.apache.pulsar.spring.boot.autoconfigure.PulsarConnectionDetails;
import org.apache.pulsar.spring.test.support.PulsarTestContainerSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.io.TempDir;
import org.testcontainers.DockerClientFactory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.docker.compose.core.DockerCompose;
import org.springframework.boot.docker.compose.core.DockerComposeFile;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

/**
 * Integration test for {@link PulsarDockerComposeConnectionDetailsFactory}.
 *
 * @author Chris Bono
 */
@EnabledIf("dockerAvailable")
class PulsarDockerComposeConnectionDetailsFactoryIntegrationTests {

	@TempDir
	private Path tempDir;

	static boolean dockerAvailable() {
		return DockerClientFactory.instance().isDockerAvailable();
	}

	@Test
	void runCreatesConnectionDetails() throws IOException {
		Path composeFile = writeComposeFile();
		SpringApplication application = new SpringApplication(TestConfiguration.class);
		application.setWebApplicationType(WebApplicationType.NONE);
		// The default shutdown runs at JVM exit, after the temporary compose file is gone
		DockerCompose dockerCompose = DockerCompose.get(DockerComposeFile.of(composeFile.toFile()), null, Set.of());
		try (ConfigurableApplicationContext context = application.run("--spring.docker.compose.skip.in-tests=false",
				"--spring.docker.compose.file=" + composeFile,
				"--spring.docker.compose.lifecycle-management=start-only")) {
			PulsarConnectionDetails connectionDetails = context.getBean(PulsarConnectionDetails.class);
			assertThat(connectionDetails.getBrokerUrl()).matches("^pulsar://\\S+:\\d+");
			assertThat(connectionDetails.getAdminUrl()).matches("^http://\\S+:\\d+");
		}
		finally {
			dockerCompose.down(Duration.ofSeconds(10));
		}
	}

	private Path writeComposeFile() throws IOException {
		String template = new ClassPathResource("pulsar-compose.yaml", getClass())
			.getContentAsString(StandardCharsets.UTF_8);
		String imageName = PulsarTestContainerSupport.getPulsarImage().asCanonicalNameString();
		return Files.writeString(this.tempDir.resolve("compose.yaml"), template.replace("{imageName}", imageName));
	}

	@Configuration(proxyBeanMethods = false)
	static class TestConfiguration {

	}

}
