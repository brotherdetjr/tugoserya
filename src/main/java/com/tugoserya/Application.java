package com.tugoserya;

import com.tugoserya.utils.SpringVerticleFactory;
import com.tugoserya.utils.WiredVerticleFactory;
import com.tugoserya.utils.Wiring;
import io.vertx.core.Vertx;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static io.vertx.core.Vertx.vertx;

public class Application {
	public static void main(String[] args) {
		ApplicationContext context = new AnnotationConfigApplicationContext(MainConfiguration.class);
		Vertx vertx = vertx();
		vertx.registerVerticleFactory(context.getBean(WiredVerticleFactory.class));
		vertx.registerVerticleFactory(context.getBean(SpringVerticleFactory.class));
		vertx.deployVerticle("wired:spring:" + MainVerticle.class.getName(), Wiring.toOptions("main"));
		vertx.deployVerticle("wired:spring:" + AdminVerticle.class.getName(), Wiring.toOptions("admin", "main"));
		vertx.deployVerticle("wired:spring:" + UserVerticle.class.getName(), Wiring.toOptions("user", "main"));
	}

}
