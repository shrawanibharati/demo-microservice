package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;

@SpringBootApplication
@EnableHystrix
public class DemoMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoMicroserviceApplication.class, args);
	}
}