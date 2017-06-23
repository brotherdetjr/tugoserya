package com.tugoserya;

import com.google.common.collect.ImmutableList;
import com.tugoserya.utils.Dependencies;
import com.tugoserya.utils.SpringVerticleFactory;
import com.tugoserya.utils.WiredVerticleFactory;
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
		vertx.deployVerticle("wired:spring:" + MainVerticle.class.getName(), Dependencies.toOptions("main"));
		vertx.deployVerticle("wired:spring:" + AdminVerticle.class.getName(), Dependencies.toOptions("admin", ImmutableList.of("main", "user")));
		vertx.deployVerticle("wired:spring:" + UserVerticle.class.getName(), Dependencies.toOptions("user", ImmutableList.of("main")));
	}

}
