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

/*
 * Copyright 2012-present the original author or authors.
 */

package org.apache.pulsar.spring.boot.testcontainers;

import org.apache.pulsar.spring.boot.autoconfigure.PulsarConnectionDetails;
import org.testcontainers.pulsar.PulsarContainer;

import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;

/**
 * {@link ContainerConnectionDetailsFactory} to create {@link PulsarConnectionDetails}
 * from a {@link ServiceConnection @ServiceConnection}-annotated {@link PulsarContainer}.
 *
 * @author Chris Bono
 */
class PulsarContainerConnectionDetailsFactory
		extends ContainerConnectionDetailsFactory<PulsarContainer, PulsarConnectionDetails> {

	@Override
	protected PulsarConnectionDetails getContainerConnectionDetails(ContainerConnectionSource<PulsarContainer> source) {
		return new PulsarContainerConnectionDetails(source);
	}

	/**
	 * {@link PulsarConnectionDetails} backed by a {@link ContainerConnectionSource}.
	 */
	private static final class PulsarContainerConnectionDetails extends ContainerConnectionDetails<PulsarContainer>
			implements PulsarConnectionDetails {

		private PulsarContainerConnectionDetails(ContainerConnectionSource<PulsarContainer> source) {
			super(source);
		}

		@Override
		public String getBrokerUrl() {
			return getContainer().getPulsarBrokerUrl();
		}

		@Override
		public String getAdminUrl() {
			return getContainer().getHttpServiceUrl();
		}

	}

}
