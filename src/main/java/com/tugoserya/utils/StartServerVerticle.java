package com.tugoserya.utils;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.springframework.beans.factory.annotation.Autowired;

public class StartServerVerticle extends AbstractVerticle {

	@Autowired
	private Router router;

	@Override
	public void start() throws Exception {
		router.route().handler(StaticHandler.create());
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
	}
}
