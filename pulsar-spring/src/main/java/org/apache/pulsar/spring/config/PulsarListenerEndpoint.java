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

package org.apache.pulsar.spring.config;

import java.util.Properties;

import org.apache.pulsar.spring.listener.AckMode;
import org.apache.pulsar.spring.listener.PulsarMessageListenerContainer;
import org.jspecify.annotations.Nullable;

/**
 * Model for a Pulsar listener endpoint. Can be used against a
 * {@link org.apache.pulsar.spring.annotation.PulsarListenerConfigurer} to register
 * endpoints programmatically.
 *
 * @author Soby Chacko
 * @author Alexander Preuß
 * @author Vedran Pavic
 */
public interface PulsarListenerEndpoint extends ListenerEndpoint<PulsarMessageListenerContainer> {

	default boolean isBatchListener() {
		return false;
	}

	default @Nullable Properties getConsumerProperties() {
		return null;
	}

	default @Nullable AckMode getAckMode() {
		return null;
	}

}
