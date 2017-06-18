package com.tugoserya;

import com.tugoserya.services.AccountService;
import com.tugoserya.services.AccountServiceImpl;
import com.tugoserya.services.AdminService;
import com.tugoserya.services.AdminServiceImpl;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.ext.web.Router;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MainConfiguration {

	@Bean
	public Vertx vertx() {
		return Vertx.vertx();
	}

	@Bean
	public EventBus eventBus(Vertx vertx) {
		return vertx.eventBus();
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
}