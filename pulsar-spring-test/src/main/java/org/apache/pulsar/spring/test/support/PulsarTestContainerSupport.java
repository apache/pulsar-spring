/*
 * Copyright 2022-present the original author or authors.
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

package org.apache.pulsar.spring.test.support;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Objects;
import java.util.Properties;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.pulsar.PulsarContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Provides a static {@link PulsarContainer} that can be shared across test classes.
 *
 * @author Chris Bono
 * @author Kartik Shrivastava
 */
@Testcontainers(disabledWithoutDocker = true)
public interface PulsarTestContainerSupport {

	PulsarContainer PULSAR_CONTAINER = new PulsarContainer(getPulsarImage()).withEnv("PULSAR_PREFIX_advertisedAddress",
			"localhost");

	static DockerImageName getPulsarImage() {
		return DockerImageName.parse("apachepulsar/pulsar:" + getPulsarVersion());
	}

	private static String getPulsarVersion() {
		String resource = "pulsar-test-container.properties";
		try (InputStream input = PulsarTestContainerSupport.class.getResourceAsStream(resource)) {
			Properties properties = new Properties();
			properties.load(Objects.requireNonNull(input, () -> "Missing resource " + resource));
			return Objects.requireNonNull(properties.getProperty("pulsar.version"));
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	@BeforeAll
	static void startContainer() {
		PULSAR_CONTAINER.start();
	}

	static void stopContainer() {
		PULSAR_CONTAINER.stop();
	}

	static String getPulsarBrokerUrl() {
		return PULSAR_CONTAINER.getPulsarBrokerUrl();
	}

	static String getHttpServiceUrl() {
		return PULSAR_CONTAINER.getHttpServiceUrl();
	}

	static boolean isContainerStarted() {
		return PULSAR_CONTAINER.isRunning();
	}

}
