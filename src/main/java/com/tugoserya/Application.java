package com.tugoserya;

import com.tugoserya.utils.StartServerVerticle;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static io.vertx.core.Future.future;
import static io.vertx.core.Vertx.vertx;

@SpringBootApplication
public class Application {
	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
		Future<Void> mainVerticleFuture = future();
		Future<Void> adminVerticleFuture = future();
		vertx().deployVerticle("java-spring:" + MainVerticle.class.getName(), res -> mainVerticleFuture.complete());
		vertx().deployVerticle("java-spring:" + AdminVerticle.class.getName(), res -> adminVerticleFuture.complete());
		CompositeFuture.all(mainVerticleFuture, adminVerticleFuture).(f -> {
				vertx().deployVerticle("java-spring:" + StartServerVerticle.class.getName());

			}
		);
	}
}
