package br.com.andre.productservice.service;

import java.util.UUID;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import br.com.andre.core.model.ProductEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductService {

	
	private final KafkaTemplate<String, ProductEvent> kafkaTemplate;

	private static final String TOPIC = "products-created-events-topic";

	public ProductEvent createProduct(ProductEvent event) {

		String productId = UUID.randomUUID().toString();

		event.setId(productId);

		SendResult<String, ProductEvent> result = null;
		
		//Using ProducerRecord to be able to send headers with kafka message
		ProducerRecord<String, ProductEvent> producerRecord = new ProducerRecord<String, ProductEvent>
			(TOPIC, productId, event);
		producerRecord.headers().add("messageId", productId.getBytes());

		try {

			result = kafkaTemplate.send(producerRecord).get();

		} catch (Exception e) {
			log.error("[KAFKA PRODUCER] - Error while sending message to Kafka: {}. Event: {}", e, event);
		}
		
		log.info("[KAFKA PRODUCER] - Partition: {}", result.getRecordMetadata().partition());
		log.info("[KAFKA PRODUCER] - Topic: {}", result.getRecordMetadata().topic());
		log.info("[KAFKA PRODUCER] - Offset: {}", result.getRecordMetadata().offset());

		return event;
	}

}
