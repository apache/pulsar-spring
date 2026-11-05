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

package org.apache.pulsar.spring.sample.nativeimage;

import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.common.schema.SchemaType;
import org.apache.pulsar.spring.annotation.PulsarListener;
import org.apache.pulsar.spring.core.PulsarTemplate;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class SampleNativeImageApplication {

	private static final String TOPIC = "sample-native-image-topic";

	public static void main(String[] args) {
		SpringApplication.run(SampleNativeImageApplication.class, args);
	}

	@Bean
	ApplicationRunner sendMessageOnStartup(PulsarTemplate<Greeting> pulsarTemplate) {
		return (args) -> pulsarTemplate.sendAsync(TOPIC, new Greeting("Hello from a native image"),
				Schema.JSON(Greeting.class));
	}

	@PulsarListener(subscriptionName = "sample-native-image-subscription", topics = TOPIC,
			schemaType = SchemaType.JSON)
	void receiveMessage(Greeting message) {
		System.out.println("Message Received: " + message);
	}

	record Greeting(String message) {
	}

}
