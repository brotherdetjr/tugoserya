package com.tugoserya;

import io.vertx.core.AbstractVerticle;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class BootstrapVerticle extends AbstractVerticle {
	@Override
	public void start() throws Exception {
		DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
		beanFactory.registerSingleton("vertx", vertx);
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(beanFactory);
		context.register(MainConfiguration.class, VertxConfiguration.class);
		context.refresh();
	}
}
