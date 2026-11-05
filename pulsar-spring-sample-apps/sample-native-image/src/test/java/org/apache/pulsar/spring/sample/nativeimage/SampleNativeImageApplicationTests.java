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

package org.apache.pulsar.spring.sample.nativeimage;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.pulsar.spring.test.support.PulsarTestContainerSupport;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;

/**
 * Starts the application as a separate process, either with ahead-of-time processing or
 * as a native image, depending on the command that the build provides.
 */
class SampleNativeImageApplicationTests implements PulsarTestContainerSupport {

	private static final String EXPECTED_OUTPUT = "Message Received: Greeting[message=Hello from a native image]";

	@Test
	void listenerReceivesMessageSentOnStartup() throws IOException {
		ProcessBuilder builder = new ProcessBuilder(System.getProperty("sample.command").split("\n"))
			.redirectErrorStream(true);
		builder.environment().put("SPRING_PULSAR_CLIENT_SERVICE_URL", PULSAR_CONTAINER.getPulsarBrokerUrl());
		builder.environment().put("SPRING_PULSAR_ADMIN_SERVICE_URL", PULSAR_CONTAINER.getHttpServiceUrl());
		Process process = builder.start();
		List<String> output = new CopyOnWriteArrayList<>();
		Thread outputReader = new Thread(() -> readOutput(process, output));
		outputReader.setDaemon(true);
		outputReader.start();
		try {
			Awaitility.await()
				.atMost(Duration.ofMinutes(1))
				.untilAsserted(() -> assertThat(output).contains(EXPECTED_OUTPUT));
		}
		finally {
			process.destroyForcibly();
		}
	}

	private static void readOutput(Process process, List<String> output) {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
			reader.lines().forEach((line) -> {
				System.out.println(line);
				output.add(line);
			});
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

}
