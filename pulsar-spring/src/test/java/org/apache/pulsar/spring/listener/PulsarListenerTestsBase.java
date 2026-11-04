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

package org.apache.pulsar.spring.listener;

import org.apache.pulsar.client.api.PulsarClient;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.pulsar.spring.annotation.EnablePulsar;
import org.apache.pulsar.spring.annotation.PulsarListener;
import org.apache.pulsar.spring.config.ConcurrentPulsarListenerContainerFactory;
import org.apache.pulsar.spring.config.PulsarListenerContainerFactory;
import org.apache.pulsar.spring.core.ConsumerBuilderCustomizer;
import org.apache.pulsar.spring.core.DefaultPulsarClientFactory;
import org.apache.pulsar.spring.core.DefaultPulsarConsumerFactory;
import org.apache.pulsar.spring.core.DefaultPulsarProducerFactory;
import org.apache.pulsar.spring.core.PulsarAdministration;
import org.apache.pulsar.spring.core.PulsarConsumerFactory;
import org.apache.pulsar.spring.core.PulsarProducerFactory;
import org.apache.pulsar.spring.core.PulsarTemplate;
import org.apache.pulsar.spring.core.PulsarTopic;
import org.apache.pulsar.spring.core.PulsarTopicBuilder;
import org.apache.pulsar.spring.test.support.PulsarTestContainerSupport;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Provides base support for {@link PulsarListener @PulsarListener} tests.
 *
 * @author Soby Chacko
 * @author Alexander Preuß
 * @author Chris Bono
 */
@SpringJUnitConfig
@DirtiesContext
abstract class PulsarListenerTestsBase implements PulsarTestContainerSupport {

	@Autowired
	protected PulsarTemplate<String> pulsarTemplate;

	@Autowired
	protected PulsarClient pulsarClient;

	@Configuration(proxyBeanMethods = false)
	@EnablePulsar
	static class TopLevelConfig {

		@Bean
		PulsarProducerFactory<String> pulsarProducerFactory(PulsarClient pulsarClient) {
			return new DefaultPulsarProducerFactory<>(pulsarClient, "foo-1");
		}

		@Bean
		PulsarClient pulsarClient() {
			return new DefaultPulsarClientFactory(PulsarTestContainerSupport.getPulsarBrokerUrl()).createClient();
		}

		@Bean
		PulsarTemplate<String> pulsarTemplate(PulsarProducerFactory<String> pulsarProducerFactory) {
			return new PulsarTemplate<>(pulsarProducerFactory);
		}

		@Bean
		public PulsarConsumerFactory<?> pulsarConsumerFactory(PulsarClient pulsarClient,
				ObjectProvider<ConsumerBuilderCustomizer<String>> defaultConsumerCustomizersProvider) {
			return new DefaultPulsarConsumerFactory<>(pulsarClient,
					defaultConsumerCustomizersProvider.orderedStream().toList());
		}

		@Bean
		PulsarListenerContainerFactory pulsarListenerContainerFactory(
				PulsarConsumerFactory<Object> pulsarConsumerFactory) {
			return new ConcurrentPulsarListenerContainerFactory<>(pulsarConsumerFactory,
					new PulsarContainerProperties());
		}

		@Bean
		PulsarAdministration pulsarAdministration() {
			return new PulsarAdministration(PulsarTestContainerSupport.getHttpServiceUrl());
		}

		@Bean
		PulsarTopic partitionedTopic() {
			return new PulsarTopicBuilder().name("persistent://public/default/concurrency-on-pl")
				.numberOfPartitions(3)
				.build();
		}

	}

}
