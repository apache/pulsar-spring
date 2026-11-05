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

package org.apache.pulsar.spring.aot;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;

import org.apache.pulsar.client.impl.ConsumerBase;
import org.junit.jupiter.api.Test;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.TypeReference;
import org.springframework.aot.hint.predicate.RuntimeHintsPredicates;
import org.springframework.core.io.ClassPathResource;

/**
 * Tests for {@link PulsarRuntimeHints}.
 */
class PulsarRuntimeHintsTests {

	private final RuntimeHints hints = registerHints();

	private static RuntimeHints registerHints() {
		RuntimeHints hints = new RuntimeHints();
		new PulsarRuntimeHints().registerHints(hints, PulsarRuntimeHintsTests.class.getClassLoader());
		return hints;
	}

	@Test
	void typesOfTheUnshadedClientAreRegistered() {
		Stream
			.of("io.netty.buffer.AbstractByteBufAllocator", "org.asynchttpclient.config.AsyncHttpClientConfigDefaults",
					"com.github.benmanes.caffeine.cache.SSMSA")
			.forEach((type) -> assertThat(RuntimeHintsPredicates.reflection().onType(TypeReference.of(type)))
				.accepts(this.hints));
	}

	@Test
	void consumerConfigurationFieldIsRegistered() throws NoSuchFieldException {
		assertThat(RuntimeHintsPredicates.reflection().onFieldAccess(ConsumerBase.class.getDeclaredField("conf")))
			.accepts(this.hints);
	}

	@Test
	void noShadedTypesAreRegistered() {
		assertThat(this.hints.reflection().typeHints())
			.noneMatch((hint) -> hint.getType().getName().startsWith("org.apache.pulsar.shade"));
	}

	@Test
	void proxiedInterfacesExist() throws ClassNotFoundException {
		ClassLoader classLoader = getClass().getClassLoader();
		for (var proxy : this.hints.proxies().jdkProxyHints().toList()) {
			for (TypeReference type : proxy.getProxiedInterfaces()) {
				assertThat(Class.forName(type.getName(), false, classLoader)).isInterface();
			}
		}
	}

	@Test
	void resourcesExist() {
		Stream
			.of("org/asynchttpclient/config/ahc-version.properties",
					"org/asynchttpclient/config/ahc-default.properties")
			.forEach((resource) -> {
				assertThat(new ClassPathResource(resource).exists()).isTrue();
				assertThat(RuntimeHintsPredicates.resource().forResource(resource)).accepts(this.hints);
			});
	}

}
