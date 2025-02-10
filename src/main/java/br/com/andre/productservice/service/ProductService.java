package br.com.andre.productservice.service;

import br.com.andre.core.model.ProductEvent;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
@Slf4j
public class ProductService {

    @Autowired
    private KafkaTemplate<String, ProductEvent> kafkaTemplate;

    public ProductEvent createProduct(ProductEvent event) {

        String productId = UUID.randomUUID().toString();
        
        event.setId(productId);

        //Forma assincrona
        //kafkaTemplate.send("products-created-events-topic", productId, event);

        //Forma Sincrona
        CompletableFuture<SendResult<String, ProductEvent>> future =
        		kafkaTemplate.send("products-created-events-topic", productId, (ProductEvent) event);
        
        future.whenComplete((result, exception) -> {
        	
        	if (exception != null) {
        		log.error(exception.getMessage());
        	} else {
        		log.info("Assynchronous Successful: {}",result.getRecordMetadata());
        	}
        });
        
        log.info("****** esperando o completable future!");
        future.join(); //força a thread principal a esperar a secundária a completar o future
        
        try {
        	log.info("RESULTADOS: {}", future.get().toString());
        } catch (Exception e) {
        	log.error("DEU RUIM AQUI MEU CONSAGRADO!");
        }
        
        
        return event;
    }

}
