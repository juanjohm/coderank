package com.atos.coderank.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rest")
public class TestController {

	@GetMapping("/a")
	public ResponseEntity<String> test(){
		return new ResponseEntity<>("Hola", HttpStatus.OK);
	}
}
