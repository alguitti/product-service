package br.com.andre.productservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeedbackController {
	
	@GetMapping("/emailFeedback/200")
	public ResponseEntity<String> feedback200() {
		return new ResponseEntity<String>("TESTE OK", HttpStatus.OK);
	}
	
	@GetMapping("/emailFeedback/500")
	public ResponseEntity<String> feedback500() {
		return new ResponseEntity<String>("TESTE CAGOU", HttpStatus.INTERNAL_SERVER_ERROR);
	}
	

}
