package br.com.andre.productservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import br.com.andre.core.model.ProductEvent;
import br.com.andre.productservice.service.ProductService;
import lombok.extern.slf4j.Slf4j;

@Slf4j
//tells spring to create only one instance of this class for all test method
//useful when the setup code is long by helping @BeforeAll to attend all tests
@TestInstance(Lifecycle.PER_CLASS) 
@ActiveProfiles("test")
@SpringBootTest
@DirtiesContext
@EmbeddedKafka(partitions = 3, count = 3, controlledShutdown = true)//3 parts, e brokers and smooth transition when broker shutdown
public class ProductServiceIT {

	@Autowired
	private ProductService productService;
	
	@Autowired
	private EmbeddedKafkaBroker embeddedKafkaBroker;
	
	@Autowired
	private Environment environment;
	
	private KafkaMessageListenerContainer<String, ProductEvent> container;
	
	private BlockingQueue<ConsumerRecord<String, ProductEvent>> records;
	
	@BeforeAll
	void setUp() {
		log.info("configs: {}", getConsumerProperties());
		DefaultKafkaConsumerFactory<String, Object> consumerFactory = 
				new DefaultKafkaConsumerFactory<>(getConsumerProperties());
		
		ContainerProperties containerProperties = new ContainerProperties(environment.getProperty("kafka.topic"));
		container = new KafkaMessageListenerContainer<>(consumerFactory, containerProperties);
		records = new LinkedBlockingQueue<>();
		container.setupMessageListener((MessageListener<String, ProductEvent>) records::add);
		container.start();
		ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
		
	}
	
	
	@Test
	void testCreateProduct_whenGivenValidProductDetails_shouldSendToKafkaTopic() throws Exception {

		productService.createProduct(buildProductEvent());
		
		ConsumerRecord<String, ProductEvent> message = records.poll(3000, TimeUnit.MILLISECONDS);
		
		assertNotNull(message);
		assertEquals(buildProductEvent().getDescription(), message.value().getDescription());
	}
	
	private ProductEvent buildProductEvent() {
		ProductEvent event = new ProductEvent();
		event.setDescription("test description");
		event.setName("test");
		return event;
	}
	
	private Map<String, Object> getConsumerProperties() {
		return Map.of(
				ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, embeddedKafkaBroker.getBrokersAsString(),
				ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
				ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class,
				ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class,
				ConsumerConfig.GROUP_ID_CONFIG, environment.getProperty("kafka.group-id"),
				JsonDeserializer.TRUSTED_PACKAGES, environment.getProperty("kafka.trusted"),
				ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, environment.getProperty("kafka.auto-offset-reset"));
	}
	
	@AfterAll
	void tearDown() {
		container.stop();
	}
	
}
