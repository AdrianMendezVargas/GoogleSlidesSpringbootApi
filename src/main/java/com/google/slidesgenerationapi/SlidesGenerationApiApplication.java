package com.google.slidesgenerationapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@SpringBootApplication
public class SlidesGenerationApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SlidesGenerationApiApplication.class, args);
	}

	@RequestMapping("/")
	public String home(){
		return "hello default, app running!";
	}


}
