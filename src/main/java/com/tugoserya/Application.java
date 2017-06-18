package com.tugoserya;

import io.vertx.core.Vertx;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static io.vertx.core.Vertx.vertx;

@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		ConfigurableApplicationContext context = SpringApplication.run(Application.class, args);
		Vertx vertx = vertx();
		vertx.registerVerticleFactory(context.getBean(SpringVerticleFactory.class));
		vertx.deployVerticle("spring:" + MainVerticle.class.getName(), res -> {
			vertx.deployVerticle("spring:" + AdminVerticle.class.getName());
		});
	}
}
