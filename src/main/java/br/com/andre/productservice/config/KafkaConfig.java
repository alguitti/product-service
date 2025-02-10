package br.com.andre.productservice.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.Data;

@Data
@Configuration
@ConfigurationProperties(prefix = "kafka")
public class KafkaConfig {
	
	private String server;
	private String topic;
	private String deliveryTimeout;
	private String requestTimeout;
	private String acks;
	private String linger;
	private boolean idempotenceEnabled;
	private String maxInflightRequests;

}
