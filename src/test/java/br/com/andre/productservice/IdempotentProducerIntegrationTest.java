package br.com.andre.productservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import br.com.andre.core.model.ProductEvent;

@SpringBootTest
public class IdempotentProducerIntegrationTest {

	@MockitoBean
	KafkaAdmin kafkaAdmin;
	
	@Autowired
	private KafkaTemplate<String, ProductEvent> kafkaTemplate;
	
	@Test
	void testProducerConfig_whenIdempotenceEnabled_assertsIndempotentProperties() {
		
		ProducerFactory<String, ProductEvent> factory = kafkaTemplate.getProducerFactory();
		Map<String, Object> configs = factory.getConfigurationProperties();
		
		assertTrue((Boolean) configs.get(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG));
		assertEquals("all", configs.get(ProducerConfig.ACKS_CONFIG));
		
		if (configs.containsKey(ProducerConfig.RETRIES_CONFIG)) {
			assertTrue(Integer.parseInt((String) configs.get(ProducerConfig.RETRIES_CONFIG)) > 0);
		}
		
	}
	
}
