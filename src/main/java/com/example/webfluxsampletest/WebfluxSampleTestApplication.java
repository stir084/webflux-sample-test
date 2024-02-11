package com.example.webfluxsampletest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebfluxSampleTestApplication {

	public static void main(String[] args) {
		System.setProperty("reactor.netty.ioWorkerCount", "1");
		SpringApplication.run(WebfluxSampleTestApplication.class, args);
	}

}
