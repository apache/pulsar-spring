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

package org.apache.pulsar.spring.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.util.List;

import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientException;
import org.apache.pulsar.client.api.Schema;
import org.apache.pulsar.client.api.TableView;
import org.apache.pulsar.spring.test.support.PulsarTestClients;
import org.apache.pulsar.spring.test.support.PulsarTestContainerSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link DefaultPulsarTableViewFactory}.
 */
class DefaultPulsarTableViewFactoryTests implements PulsarTestContainerSupport {

	private PulsarClient pulsarClient;

	@BeforeEach
	void createPulsarClient() throws PulsarClientException {
		this.pulsarClient = PulsarTestClients.builder().serviceUrl(PulsarTestContainerSupport.getPulsarBrokerUrl()).build();
	}

	@AfterEach
	void closePulsarClient() throws PulsarClientException {
		if (this.pulsarClient != null && !this.pulsarClient.isClosed()) {
			this.pulsarClient.close();
		}
	}

	private void publishKeyedMessages(String topic) {
		var pulsarTemplate = new PulsarTemplate<>(new DefaultPulsarProducerFactory<String>(this.pulsarClient, topic));
		pulsarTemplate.newMessage("one").withMessageCustomizer((builder) -> builder.key("key-1")).send();
		pulsarTemplate.newMessage("two").withMessageCustomizer((builder) -> builder.key("key-2")).send();
		pulsarTemplate.newMessage("one-updated").withMessageCustomizer((builder) -> builder.key("key-1")).send();
	}

	@Nested
	class BasicScenarioTests {

		private DefaultPulsarTableViewFactory<String> tableViewFactory;

		@BeforeEach
		void createTableViewFactory() {
			this.tableViewFactory = new DefaultPulsarTableViewFactory<>(pulsarClient);
		}

		@Test
		void tableViewHoldsTheLatestValueOfEachKey() throws Exception {
			var topic = "dptvf-basic-topic";
			publishKeyedMessages(topic);
			try (TableView<String> tableView = this.tableViewFactory.createTableView(topic, Schema.STRING, null)) {
				await().atMost(Duration.ofSeconds(10)).untilAsserted(() -> {
					assertThat(tableView.size()).isEqualTo(2);
					assertThat(tableView.get("key-1")).isEqualTo("one-updated");
					assertThat(tableView.get("key-2")).isEqualTo("two");
				});
			}
		}

		@Test
		void tableViewFollowsTheTopic() throws Exception {
			var topic = "dptvf-follow-topic";
			try (TableView<String> tableView = this.tableViewFactory.createTableView(topic, Schema.STRING, null)) {
				assertThat(tableView.isEmpty()).isTrue();
				publishKeyedMessages(topic);
				await().atMost(Duration.ofSeconds(10))
					.untilAsserted(() -> assertThat(tableView.get("key-1")).isEqualTo("one-updated"));
			}
		}

		@Test
		void useFactoryDefaults() throws Exception {
			var topic = "dptvf-defaults-topic";
			this.tableViewFactory = new DefaultPulsarTableViewFactory<>(pulsarClient,
					List.of((builder) -> builder.topic(topic)));
			publishKeyedMessages(topic);
			// The following code expects the above topic to be used
			try (TableView<String> tableView = this.tableViewFactory.createTableView(null, Schema.STRING, null)) {
				await().atMost(Duration.ofSeconds(10))
					.untilAsserted(() -> assertThat(tableView.get("key-2")).isEqualTo("two"));
			}
		}

	}

	@Nested
	class WithPulsarTopicBuilder {

		@Test
		void topicIsFullyQualified() throws Exception {
			var topic = "dptvf-wptb-topic";
			var tableViewFactory = new DefaultPulsarTableViewFactory<String>(pulsarClient);
			tableViewFactory.setTopicBuilder(new PulsarTopicBuilder());
			publishKeyedMessages(topic);
			try (TableView<String> tableView = tableViewFactory.createTableView(topic, Schema.STRING, null)) {
				await().atMost(Duration.ofSeconds(10))
					.untilAsserted(() -> assertThat(tableView.get("key-2")).isEqualTo("two"));
			}
		}

	}

}
