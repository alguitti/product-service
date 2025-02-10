package br.com.andre.productservice.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.andre.core.model.ProductEvent;
import br.com.andre.productservice.service.ProductService;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @PostMapping
    public ResponseEntity<ProductEvent> createProduct(@RequestBody ProductEvent product) {
    	
    	
    	ProductEvent event = productService.createProduct(product);
    	return new ResponseEntity<>(event,HttpStatus.CREATED);
    	
    }



}
