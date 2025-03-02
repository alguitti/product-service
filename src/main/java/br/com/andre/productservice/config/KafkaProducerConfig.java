package br.com.andre.productservice.config;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.transaction.KafkaTransactionManager;

import br.com.andre.core.model.ProductEvent;


@Configuration
public class KafkaProducerConfig {
	
	@Autowired
	private KafkaConfig kafkaConfig;

    @Bean
    public NewTopic createTopic() {
        return TopicBuilder.name("products-created-events-topic")
                .partitions(3)
                .replicas(3) //que replica nos diferentes brokers do cluster
                .configs(Map.of("min.insync.replicas","2")) //especifica o minimo de replicas para aceitar a publicação
                .build();
    }
    
    public Map<String, Object> producerConfigs() {
    	
    	Map<String, Object> configs = new HashMap<>();
    	configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getServer());
    	configs.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, kafkaConfig.getDeliveryTimeout());
    	configs.put(ProducerConfig.ACKS_CONFIG, kafkaConfig.getAcks());
    	configs.put(ProducerConfig.LINGER_MS_CONFIG, kafkaConfig.getLinger());
    	configs.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaConfig.getRequestTimeout());
    	configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    	configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    	configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, kafkaConfig.isIdempotenceEnabled());
    	configs.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, kafkaConfig.getMaxInflightRequests());
    	configs.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, kafkaConfig.getTransactionId());
    	
    	return configs;
    }
    
    @Bean
    public ProducerFactory<String, ProductEvent> producerFactory() {
    	return new DefaultKafkaProducerFactory<String, ProductEvent>(producerConfigs());
    }
    
    @Bean
    public KafkaTemplate<String, ProductEvent> createTemplate() {
    	return new KafkaTemplate<String, ProductEvent>(producerFactory());
    }
    
    @Bean
    public KafkaTransactionManager<String, ProductEvent> kafkaTransactionManager() {
    	return new KafkaTransactionManager<>(producerFactory());
    }

}
