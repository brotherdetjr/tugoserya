package com.tugoserya;

import com.tugoserya.services.AccountService;
import com.tugoserya.services.AccountServiceImpl;
import com.tugoserya.services.AdminService;
import com.tugoserya.services.AdminServiceImpl;
import com.tugoserya.utils.Dependencies;
import com.tugoserya.utils.SpringVerticleFactory;
import com.tugoserya.utils.WiredVerticleFactory;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.Router;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MainConfiguration {

	@Bean
	public SpringVerticleFactory springVerticleFactory() {
		return new SpringVerticleFactory();
	}

	@Bean
	public WiredVerticleFactory wiredVerticleFactory(Dependencies dependencies) {
		return new WiredVerticleFactory(dependencies);
	}

	@Bean
	public Vertx vertx() {
		return Vertx.vertx();
	}

	@Bean
	public AdminService adminService() {
		return new AdminServiceImpl();
	}

	@Bean
	public AccountService accountService() {
		return new AccountServiceImpl();
	}

	@Bean
	public Router router(Vertx vertx) {
		return Router.router(vertx);
	}

	@Bean
	public EventBus eventBus(Vertx vertx) {
		return vertx.eventBus();
	}

	@Bean
	public Dependencies dependencies(EventBus eventBus) {
		return new Dependencies(eventBus, "dependencies:verticles");
	}
}
