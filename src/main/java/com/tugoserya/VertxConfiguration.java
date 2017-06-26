package com.tugoserya;

import com.tugoserya.utils.SpringVerticleFactory;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.Router;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VertxConfiguration {

	@Bean
	public SpringVerticleFactory springVerticleFactory() {
		return new SpringVerticleFactory();
	}

	@Bean
	public Router router(Vertx vertx) {
		return Router.router(vertx);
	}

	@Bean
	public EventBus eventBus(Vertx vertx) {
		return vertx.eventBus();
	}
}
