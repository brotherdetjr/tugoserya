package com.tugoserya.utils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.VerticleFactory;

import static java.util.Objects.requireNonNull;

public class WiredVerticleFactory implements VerticleFactory {

	private final Dependencies dependencies;

	public WiredVerticleFactory(Dependencies dependencies) {
		this.dependencies = dependencies;
	}

	@Override
	public String prefix() {
		return "wired";
	}

	@Override
	public Verticle createVerticle(String verticleName, ClassLoader classLoader) throws Exception {
		return new BootstrapVerticle(verticleName);
	}

	public class BootstrapVerticle extends AbstractVerticle {

		private final String verticleName;

		public BootstrapVerticle(String verticleName) {
			this.verticleName = verticleName;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void start() throws Exception {
			JsonObject config = vertx.getOrCreateContext().config();
			JsonArray deps = config.getJsonArray("wired.deps");
			String name = config.getString("wired.name");
			requireNonNull(name);
			if (deps != null) {
				dependencies.having(deps.getList()).register(name).map(ignore -> { deployVerticle(); return null; });
			} else {
				dependencies.register(name);
				deployVerticle();
			}
		}

		private void deployVerticle() {
			vertx.deployVerticle(
				VerticleFactory.removePrefix(verticleName),
				new DeploymentOptions(vertx.getOrCreateContext().config()),
				ignore2 -> vertx.undeploy(verticleName)
			);
		}
	}
}
