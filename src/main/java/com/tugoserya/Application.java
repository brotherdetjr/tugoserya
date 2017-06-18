package com.tugoserya;

import com.tugoserya.utils.SpringVerticleFactory;
import io.vertx.core.Vertx;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static io.vertx.core.Vertx.vertx;

public class Application {
	public static void main(String[] args) {
		ApplicationContext context = new AnnotationConfigApplicationContext(MainConfiguration.class);
		Vertx vertx = vertx();
		vertx.registerVerticleFactory(context.getBean(SpringVerticleFactory.class));
		vertx.deployVerticle("spring:" + MainVerticle.class.getName(), res -> {
			vertx.deployVerticle("spring:" + AdminVerticle.class.getName());
			vertx.deployVerticle("spring:" + UserVerticle.class.getName());
		});
	}
}
