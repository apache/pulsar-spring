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

import org.apache.pulsar.client.api.ClientBuilder;
import org.apache.pulsar.client.api.PulsarClient;
import org.apache.pulsar.client.api.PulsarClientSharedResources;

/**
 * Creates Pulsar client builders for tests that create many clients.
 * <p>
 * The clients share their thread pools and other resources. Closing a client that has its
 * own event loops takes about two seconds, since Netty waits for a quiet period when
 * shutting them down, which adds up in tests that create a client for each test method.
 *
 * @since 3.0.0
 */
public final class PulsarTestClients {

	private static final PulsarClientSharedResources SHARED_RESOURCES = PulsarClientSharedResources.builder().build();

	private PulsarTestClients() {
	}

	/**
	 * Create a client builder whose clients share their resources.
	 * @return the client builder
	 */
	public static ClientBuilder builder() {
		return PulsarClient.builder().sharedResources(SHARED_RESOURCES);
	}

}
