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

package org.apache.pulsar.spring.cloud.stream.binder.config;

import org.apache.pulsar.spring.boot.autoconfigure.PulsarProperties;
import org.apache.pulsar.spring.cloud.stream.binder.PulsarMessageChannelBinder;
import org.apache.pulsar.spring.cloud.stream.binder.properties.PulsarBinderConfigurationProperties;
import org.apache.pulsar.spring.cloud.stream.binder.properties.PulsarExtendedBindingProperties;
import org.apache.pulsar.spring.cloud.stream.binder.provisioning.PulsarTopicProvisioner;
import org.apache.pulsar.spring.core.PulsarAdministration;
import org.apache.pulsar.spring.core.PulsarConsumerFactory;
import org.apache.pulsar.spring.core.PulsarTemplate;
import org.apache.pulsar.spring.core.PulsarTopicBuilder;
import org.apache.pulsar.spring.core.SchemaResolver;
import org.apache.pulsar.spring.support.header.JacksonUtils;
import org.apache.pulsar.spring.support.header.JsonPulsarHeaderMapper;
import org.apache.pulsar.spring.support.header.PulsarHeaderMapper;
import org.apache.pulsar.spring.support.header.ToStringPulsarHeaderMapper;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.stream.binder.Binder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Pulsar binder {@link Configuration}.
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnMissingBean(Binder.class)
@EnableConfigurationProperties({ PulsarProperties.class, PulsarExtendedBindingProperties.class,
		PulsarBinderConfigurationProperties.class })
public class PulsarBinderConfiguration {

	@Bean
	public PulsarTopicProvisioner pulsarTopicProvisioner(PulsarAdministration pulsarAdministration,
			PulsarBinderConfigurationProperties pulsarBinderConfigurationProperties, PulsarTopicBuilder topicBuilder) {
		return new PulsarTopicProvisioner(pulsarAdministration, pulsarBinderConfigurationProperties, topicBuilder);
	}

	@Bean
	@ConditionalOnMissingBean
	public PulsarHeaderMapper pulsarHeaderMapper() {
		if (JacksonUtils.isJacksonPresent()) {
			return JsonPulsarHeaderMapper.builder().build();
		}
		return new ToStringPulsarHeaderMapper();
	}

	@Bean
	public PulsarMessageChannelBinder pulsarMessageChannelBinder(PulsarTopicProvisioner pulsarTopicProvisioner,
			PulsarTemplate<Object> pulsarTemplate, PulsarConsumerFactory<?> pulsarConsumerFactory,
			PulsarBinderConfigurationProperties binderConfigProps, PulsarExtendedBindingProperties bindingConfigProps,
			SchemaResolver schemaResolver, PulsarHeaderMapper headerMapper) {
		PulsarMessageChannelBinder pulsarMessageChannelBinder = new PulsarMessageChannelBinder(pulsarTopicProvisioner,
				pulsarTemplate, pulsarConsumerFactory, binderConfigProps, schemaResolver, headerMapper);
		pulsarMessageChannelBinder.setExtendedBindingProperties(bindingConfigProps);
		return pulsarMessageChannelBinder;
	}

}
