/*
 * Copyright 2023-present the original author or authors.
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

package org.apache.pulsar.spring.cloud.stream.binder;

import org.apache.pulsar.spring.cloud.stream.binder.properties.PulsarBinderConfigurationProperties;
import org.apache.pulsar.spring.cloud.stream.binder.provisioning.PulsarTopicProvisioner;
import org.apache.pulsar.spring.core.PulsarConsumerFactory;
import org.apache.pulsar.spring.core.PulsarTemplate;
import org.apache.pulsar.spring.core.SchemaResolver;
import org.apache.pulsar.spring.support.header.PulsarHeaderMapper;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.config.EnableIntegration;

/**
 * Test binder to exercise producer/consumer bindings in
 * {@link PulsarMessageChannelBinder}.
 */
class PulsarTestBinder extends AbstractPulsarTestBinder {

	@SuppressWarnings({ "unchecked" })
	PulsarTestBinder(PulsarTopicProvisioner pulsarTopicProvisioner, PulsarTemplate<?> pulsarTemplate,
			PulsarConsumerFactory<?> pulsarConsumerFactory, PulsarBinderConfigurationProperties binderConfigProps,
			SchemaResolver schemaResolver, PulsarHeaderMapper headerMapper) {

		try {
			var binder = new PulsarMessageChannelBinder(pulsarTopicProvisioner, (PulsarTemplate<Object>) pulsarTemplate,
					pulsarConsumerFactory, binderConfigProps, schemaResolver, headerMapper);
			var context = new AnnotationConfigApplicationContext(Config.class);
			setApplicationContext(context);
			binder.setApplicationContext(context);
			binder.afterPropertiesSet();
			this.setPollableConsumerBinder(binder);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Configuration
	@EnableIntegration
	static class Config {

	}

}
