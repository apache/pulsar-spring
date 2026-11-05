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

package org.apache.pulsar.spring.aot;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.TreeMap;
import java.util.stream.Stream;

import org.apache.pulsar.client.admin.ListTopicsOptions;
import org.apache.pulsar.client.admin.internal.OffloadProcessStatusImpl;
import org.apache.pulsar.client.admin.internal.PulsarAdminBuilderImpl;
import org.apache.pulsar.client.api.Authentication;
import org.apache.pulsar.client.api.AuthenticationDataProvider;
import org.apache.pulsar.client.impl.ConsumerBase;
import org.apache.pulsar.client.impl.conf.ClientConfigurationData;
import org.apache.pulsar.client.impl.conf.ConsumerConfigurationData;
import org.apache.pulsar.client.impl.conf.ProducerConfigurationData;
import org.apache.pulsar.client.util.SecretsSerializer;
import org.apache.pulsar.common.protocol.Commands;
import org.jspecify.annotations.Nullable;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.ReflectionHints;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.TypeReference;

/**
 * {@link RuntimeHintsRegistrar} for Apache Pulsar support for Spring.
 *
 * @author Soby Chacko
 * @author Chris Bono
 */
public class PulsarRuntimeHints implements RuntimeHintsRegistrar {

	@Override
	public void registerHints(RuntimeHints hints, @Nullable ClassLoader classLoader) {
		ReflectionHints reflectionHints = hints.reflection();
		// The following components need access to declared constructors, invoke declared
		// methods and introspect all public methods. The components are a mix of JDK
		// classes and core Pulsar classes.
		Stream
			.of(HashSet.class, LinkedHashMap.class, TreeMap.class, Authentication.class,
					AuthenticationDataProvider.class, SecretsSerializer.class, PulsarAdminBuilderImpl.class,
					OffloadProcessStatusImpl.class, Commands.class)
			.forEach(type -> reflectionHints.registerType(type, builder -> builder
				.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS, MemberCategory.INVOKE_DECLARED_METHODS)));

		// In addition to the above member category levels, these components need field
		// and declared class level access.
		Stream
			.of(ClientConfigurationData.class, ConsumerConfigurationData.class, ProducerConfigurationData.class,
					ListTopicsOptions.class)
			.forEach(type -> reflectionHints.registerType(type,
					builder -> builder.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
							MemberCategory.INVOKE_DECLARED_METHODS, MemberCategory.ACCESS_DECLARED_FIELDS)));

		// DefaultPulsarMessageListenerContainer reads the configuration of the consumer
		reflectionHints.registerType(ConsumerBase.class, (builder) -> builder.withField("conf"));

		// @formatter:off
		// These are classes of the libraries of the Pulsar client and other inaccessible
		// interfaces/classes (thus using string version of API).
		Stream.of("org.apache.pulsar.client.admin.internal.JacksonConfigurator",
				"org.apache.pulsar.client.impl.conf.TopicConsumerConfigurationData",
				"org.apache.pulsar.client.impl.conf.TopicConsumerConfigurationData$TopicNameMatcher",
				"org.apache.pulsar.client.util.SecretsSerializer",
				"org.apache.pulsar.common.classification.InterfaceAudience$Public",
				"org.apache.pulsar.common.protocol.ByteBufPair$Encoder",
				"org.apache.pulsar.common.protocol.PulsarDecoder",
				"org.apache.pulsar.common.util.netty.DnsResolverUtil",
				"com.fasterxml.jackson.databind.ext.Java7HandlersImpl",
				"com.fasterxml.jackson.databind.ext.Java7SupportImpl",
				"com.google.common.util.concurrent.AbstractFuture",
				"io.netty.buffer.AbstractByteBufAllocator",
				"io.netty.buffer.AbstractReferenceCountedByteBuf",
				"io.netty.channel.ChannelDuplexHandler",
				"io.netty.channel.ChannelHandlerAdapter",
				"io.netty.channel.ChannelInboundHandlerAdapter",
				"io.netty.channel.ChannelInitializer",
				"io.netty.channel.ChannelOutboundHandlerAdapter",
				"io.netty.channel.CombinedChannelDuplexHandler",
				"io.netty.channel.DefaultChannelPipeline$HeadContext",
				"io.netty.channel.DefaultChannelPipeline$TailContext",
				"io.netty.channel.DefaultFileRegion",
				"io.netty.channel.epoll.NativeDatagramPacketArray$NativeDatagramPacket",
				"io.netty.channel.socket.nio.NioDatagramChannel",
				"io.netty.channel.socket.nio.NioSocketChannel",
				"io.netty.channel.unix.PeerCredentials",
				"io.netty.handler.codec.ByteToMessageDecoder",
				"io.netty.handler.codec.LengthFieldBasedFrameDecoder",
				"io.netty.handler.codec.MessageToMessageEncoder",
				"io.netty.handler.codec.dns.DatagramDnsQueryEncoder",
				"io.netty.handler.codec.http.HttpClientCodec",
				"io.netty.handler.codec.http.HttpContentDecoder",
				"io.netty.handler.codec.http.HttpContentDecompressor",
				"io.netty.handler.flush.FlushConsolidationHandler",
				"io.netty.handler.stream.ChunkedWriteHandler",
				"io.netty.resolver.dns.DnsNameResolver$1",
				"io.netty.resolver.dns.DnsNameResolver$3",
				"io.netty.resolver.dns.DnsNameResolver$DnsResponseHandler",
				"io.netty.util.AbstractReferenceCounted",
				"io.netty.util.ReferenceCountUtil",
				"io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueueColdProducerFields",
				"io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueueConsumerFields",
				"io.netty.util.internal.shaded.org.jctools.queues.BaseMpscLinkedArrayQueueProducerFields",
				"io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueConsumerIndexField",
				"io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueProducerIndexField",
				"io.netty.util.internal.shaded.org.jctools.queues.MpscArrayQueueProducerLimitField",
				"io.netty.util.internal.shaded.org.jctools.queues.unpadded.MpscUnpaddedArrayQueueConsumerIndexField",
				"io.netty.util.internal.shaded.org.jctools.queues.unpadded.MpscUnpaddedArrayQueueProducerIndexField",
				"io.netty.util.internal.shaded.org.jctools.queues.unpadded.MpscUnpaddedArrayQueueProducerLimitField",
				"org.asynchttpclient.config.AsyncHttpClientConfigDefaults",
				"org.asynchttpclient.config.AsyncHttpClientConfigHelper",
				"org.asynchttpclient.netty.channel.ChannelManager$1",
				"org.asynchttpclient.netty.handler.AsyncHttpClientHandler",
				"org.asynchttpclient.netty.handler.HttpHandler",
				"org.glassfish.hk2.api.DynamicConfigurationService",
				"org.glassfish.hk2.internal.PerThreadContext",
				"org.glassfish.hk2.utilities.ServiceLocatorUtilities",
				"org.glassfish.jersey.client.ChunkedInputReader",
				"org.glassfish.jersey.client.ClientAsyncExecutor",
				"org.glassfish.jersey.client.ClientBackgroundScheduler",
				"org.glassfish.jersey.client.DefaultClientAsyncExecutorProvider",
				"org.glassfish.jersey.client.DefaultClientBackgroundSchedulerProvider",
				"org.glassfish.jersey.client.JerseyClientBuilder",
				"org.glassfish.jersey.inject.hk2.ContextInjectionResolverImpl",
				"org.glassfish.jersey.inject.hk2.Hk2InjectionManagerFactory",
				"org.glassfish.jersey.inject.hk2.Hk2RequestScope",
				"org.glassfish.jersey.inject.hk2.InstanceSupplierFactoryBridge",
				"org.glassfish.jersey.inject.hk2.JerseyErrorService",
				"org.glassfish.jersey.inject.hk2.RequestContext",
				"org.glassfish.jersey.internal.JaxrsProviders",
				"org.glassfish.jersey.internal.RuntimeDelegateImpl",
				"org.glassfish.jersey.internal.inject.Custom",
				"org.glassfish.jersey.jackson.JacksonFeature",
				"org.glassfish.jersey.jackson.internal.DefaultJacksonJaxbJsonProvider",
				"org.glassfish.jersey.jackson.internal.JacksonAutoDiscoverable",
				"org.glassfish.jersey.jackson.internal.jackson.jaxrs.base.ProviderBase",
				"org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider",
				"org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJsonProvider",
				"org.glassfish.jersey.logging.LoggingFeatureAutoDiscoverable",
				"org.glassfish.jersey.media.multipart.MultiPartFeature",
				"org.glassfish.jersey.media.multipart.internal.MultiPartReaderClientSide",
				"org.glassfish.jersey.media.multipart.internal.MultiPartWriter",
				"org.glassfish.jersey.message.internal.AbstractFormProvider",
				"org.glassfish.jersey.message.internal.AbstractMessageReaderWriterProvider",
				"org.glassfish.jersey.message.internal.BasicTypesMessageProvider",
				"org.glassfish.jersey.message.internal.ByteArrayProvider",
				"org.glassfish.jersey.message.internal.DataSourceProvider",
				"org.glassfish.jersey.message.internal.EnumMessageProvider",
				"org.glassfish.jersey.message.internal.FileProvider",
				"org.glassfish.jersey.message.internal.FormMultivaluedMapProvider",
				"org.glassfish.jersey.message.internal.FormProvider",
				"org.glassfish.jersey.message.internal.InputStreamProvider",
				"org.glassfish.jersey.message.internal.ReaderProvider",
				"org.glassfish.jersey.message.internal.RenderedImageProvider",
				"org.glassfish.jersey.message.internal.SourceProvider",
				"org.glassfish.jersey.message.internal.SourceProvider$DomSourceReader",
				"org.glassfish.jersey.message.internal.SourceProvider$SaxSourceReader",
				"org.glassfish.jersey.message.internal.SourceProvider$SourceWriter",
				"org.glassfish.jersey.message.internal.SourceProvider$StreamSourceReader",
				"org.glassfish.jersey.message.internal.StreamingOutputProvider",
				"org.glassfish.jersey.message.internal.StringMessageProvider",
				"org.glassfish.jersey.process.internal.RequestScope",
				"org.glassfish.jersey.spi.AbstractThreadPoolProvider",
				"org.glassfish.jersey.spi.ScheduledThreadPoolExecutorProvider",
				"org.glassfish.jersey.spi.ThreadPoolExecutorProvider",
				"org.jvnet.hk2.internal.DynamicConfigurationServiceImpl",
				"org.jvnet.hk2.internal.ServiceLocatorRuntimeImpl",
				"com.github.benmanes.caffeine.cache.PSAMS",
				"com.github.benmanes.caffeine.cache.PSW",
				"com.github.benmanes.caffeine.cache.PSWMS",
				"com.github.benmanes.caffeine.cache.SSLA",
				"com.github.benmanes.caffeine.cache.SSMSA",
				"com.github.benmanes.caffeine.cache.SSLMSAW")
			.forEach(type -> reflectionHints.registerTypeIfPresent(classLoader, type,
					builder -> builder.withMembers(MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
							MemberCategory.INVOKE_PUBLIC_METHODS, MemberCategory.INVOKE_DECLARED_METHODS,
							MemberCategory.ACCESS_DECLARED_FIELDS)));
		// @formatter:on

		// Registering JDK dynamic proxies for these interfaces. Since the Connection
		// interface is protected, we need to use the string version of proxy
		// registration.
		// Although the other interfaces are public, due to ConnectionHandler$Connection
		// being protected forces all of them to be registered using the string version
		// of the API because all of them need to be registered through a single call.
		hints.proxies()
			.registerJdkProxy(TypeReference.of("io.netty.util.TimerTask"),
					TypeReference.of("org.apache.pulsar.client.impl.ConnectionHandler$Connection"),
					TypeReference.of("org.apache.pulsar.client.api.Producer"),
					TypeReference.of("org.springframework.aop.SpringProxy"),
					TypeReference.of("org.springframework.aop.framework.Advised"),
					TypeReference.of("org.springframework.core.DecoratingProxy"));

		// Register required properties files
		hints.resources().registerPattern("org/asynchttpclient/config/ahc-version.properties");
		hints.resources().registerPattern("org/asynchttpclient/config/ahc-default.properties");
	}

}
