package com.tugoserya.utils;

import io.vertx.core.Verticle;
import io.vertx.core.spi.VerticleFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import static io.vertx.core.spi.VerticleFactory.removePrefix;

public class SpringVerticleFactory implements VerticleFactory, ApplicationContextAware {

	private ApplicationContext applicationContext;

	@Override
	public String prefix() {
		return "spring";
	}

	@Override
	public Verticle createVerticle(String verticleName, ClassLoader classLoader) throws Exception {
		try {
			Class<?> clazz = classLoader.loadClass(removePrefix(verticleName));
			Verticle verticle = (Verticle) clazz.newInstance();
			applicationContext.getAutowireCapableBeanFactory().autowireBean(verticle);
			return verticle;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}
}
