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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.Objects;
import java.util.Properties;

import org.testcontainers.pulsar.PulsarContainer;
import org.testcontainers.utility.DockerImageName;

/**
 * Creates {@link PulsarContainer Pulsar containers} that are configured for tests.
 * <p>
 * The containers advertise {@code localhost}, so that a client on the host can reach the
 * broker, and allow for a slow start with two startup attempts and a timeout of three
 * minutes. They are not started, and can be customized further.
 *
 * @since 3.0.0
 */
public final class PulsarTestImage {

	/**
	 * The system property that sets the {@link #getDefaultImage() default image}.
	 */
	public static final String IMAGE_SYSTEM_PROPERTY = "pulsar.test.image";

	/**
	 * The environment variable that sets the {@link #getDefaultImage() default image},
	 * unless {@value #IMAGE_SYSTEM_PROPERTY} is set.
	 */
	public static final String IMAGE_ENVIRONMENT_VARIABLE = "PULSAR_TEST_IMAGE";

	private static final String IMAGE_NAME = "apachepulsar/pulsar";

	private PulsarTestImage() {
	}

	/**
	 * Create a container with the {@link #getDefaultImage() default image}.
	 * @return the container, which has not been started
	 */
	public static PulsarContainer createContainer() {
		return createContainer(getDefaultImage());
	}

	/**
	 * Create a container with the given tag of the {@code apachepulsar/pulsar} image.
	 * @param tag the image tag, such as {@code 5.0.1}
	 * @return the container, which has not been started
	 */
	public static PulsarContainer createContainer(String tag) {
		return createContainer(DockerImageName.parse(IMAGE_NAME).withTag(tag));
	}

	/**
	 * Create a container with the given image, which is assumed to be compatible with
	 * {@code apachepulsar/pulsar}.
	 * @param image the image
	 * @return the container, which has not been started
	 */
	public static PulsarContainer createContainer(DockerImageName image) {
		return new PulsarContainer(image.asCompatibleSubstituteFor(IMAGE_NAME)).withStartupAttempts(2)
			.withStartupTimeout(Duration.ofMinutes(3))
			.withEnv("PULSAR_PREFIX_advertisedAddress", "localhost");
	}

	/**
	 * Return the default image. This is the image in the {@value #IMAGE_SYSTEM_PROPERTY}
	 * system property or the {@value #IMAGE_ENVIRONMENT_VARIABLE} environment variable,
	 * such as {@code apachepulsar/pulsar:5.0.1}, which is assumed to be compatible with
	 * {@code apachepulsar/pulsar}. When neither is set, it is the
	 * {@code apachepulsar/pulsar} image of the Pulsar version that this project is built
	 * with.
	 * @return the default image
	 */
	public static DockerImageName getDefaultImage() {
		String image = System.getProperty(IMAGE_SYSTEM_PROPERTY);
		if (image == null || image.isBlank()) {
			image = System.getenv(IMAGE_ENVIRONMENT_VARIABLE);
		}
		if (image == null || image.isBlank()) {
			return DockerImageName.parse(IMAGE_NAME).withTag(getDefaultTag());
		}
		return DockerImageName.parse(image.strip()).asCompatibleSubstituteFor(IMAGE_NAME);
	}

	private static String getDefaultTag() {
		String resource = "pulsar-test-container.properties";
		try (InputStream input = PulsarTestImage.class.getResourceAsStream(resource)) {
			Properties properties = new Properties();
			properties.load(Objects.requireNonNull(input, () -> "Missing resource " + resource));
			return Objects.requireNonNull(properties.getProperty("pulsar.version"));
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

}
