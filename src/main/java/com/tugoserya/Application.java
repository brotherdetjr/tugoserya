package com.tugoserya;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static io.vertx.core.Vertx.vertx;

@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		vertx().deployVerticle("java-spring:" + MainVerticle.class.getName());
		vertx().deployVerticle("java-spring:" + AdminVerticle.class.getName());
	}
}
