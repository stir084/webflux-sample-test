package com.example.webfluxsampletest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WebfluxSampleTestApplication {
	public static String number;
	public static void main(String[] args) {
		System.setProperty("reactor.netty.ioWorkerCount", "1");

		StringBuilder numberString = new StringBuilder();

		// 1부터 9까지의 숫자를 추가합니다.
		for (int i = 1; i <= 9; i++) {
			numberString.append(i);
		}

		// 10부터 999999까지의 숫자를 추가합니다.
		for (int i = 10; i <= 9999999; i++) {
			numberString.append(String.valueOf("ㅇ"));
		}
		number = numberString.toString();
		SpringApplication.run(WebfluxSampleTestApplication.class, args);
	}

}
