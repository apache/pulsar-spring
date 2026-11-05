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
	 * Create a container with the given image. An image that is not named
	 * {@code apachepulsar/pulsar} must be declared compatible with it, by using
	 * {@link DockerImageName#asCompatibleSubstituteFor(String)}.
	 * @param image the image
	 * @return the container, which has not been started
	 */
	public static PulsarContainer createContainer(DockerImageName image) {
		return new PulsarContainer(image).withStartupAttempts(2)
			.withStartupTimeout(Duration.ofMinutes(3))
			.withEnv("PULSAR_PREFIX_advertisedAddress", "localhost");
	}

	/**
	 * Return the default image, which is the {@code apachepulsar/pulsar} image of the
	 * Pulsar version that this project is built with.
	 * @return the default image
	 */
	public static DockerImageName getDefaultImage() {
		return DockerImageName.parse(IMAGE_NAME).withTag(getDefaultTag());
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
