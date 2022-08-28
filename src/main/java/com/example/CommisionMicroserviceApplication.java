package com.example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.hystrix.EnableHystrix;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@SpringBootApplication
@EnableHystrix
public class CommisionMicroserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CommisionMicroserviceApplication.class, args);
	}
}