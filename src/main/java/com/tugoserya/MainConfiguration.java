package com.tugoserya;

import com.tugoserya.services.AccountService;
import com.tugoserya.services.AccountServiceImpl;
import com.tugoserya.services.AdminService;
import com.tugoserya.services.AdminServiceImpl;
import com.tugoserya.utils.SpringVerticleFactory;
import com.tugoserya.utils.WiredVerticleFactory;
import com.tugoserya.utils.Wiring;
import io.vertx.core.Vertx;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MainConfiguration {

	@Bean
	public Vertx initializedVertx(Vertx vertx, SpringVerticleFactory springVerticleFactory) {
		vertx.registerVerticleFactory(new WiredVerticleFactory(new Wiring(vertx.eventBus(), "wired:verticles")));
		vertx.registerVerticleFactory(springVerticleFactory);
		vertx.deployVerticle("wired:spring:" + MainVerticle.class.getName(), Wiring.toOptions("main"));
		vertx.deployVerticle("wired:spring:" + AdminVerticle.class.getName(), Wiring.toOptions("admin", "main"));
		vertx.deployVerticle("wired:spring:" + UserVerticle.class.getName(), Wiring.toOptions("user", "main"));
		return vertx;
	}

	@Bean
	public AdminService adminService() {
		return new AdminServiceImpl();
	}

	@Bean
	public AccountService accountService() {
		return new AccountServiceImpl();
	}

}
