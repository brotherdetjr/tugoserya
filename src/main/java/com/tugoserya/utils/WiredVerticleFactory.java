package com.tugoserya.utils;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Verticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.spi.VerticleFactory;
import org.slf4j.Logger;

import java.util.List;
import java.util.function.Consumer;

import static org.slf4j.LoggerFactory.getLogger;

public class WiredVerticleFactory implements VerticleFactory {

	private static final Logger log = getLogger(WiredVerticleFactory.class);

	private final Wiring wiring;

	public WiredVerticleFactory(Wiring wiring) {
		this.wiring = wiring;
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

		@Override
		public void start() throws Exception {
			JsonArray deps = config().getJsonArray("wired.deps");
			if (deps != null && !deps.isEmpty()) {
				@SuppressWarnings("unchecked") List<String> depsList = deps.getList();
				wiring.waitFor(depsList).map(this::deployVerticle);
			} else {
				deployVerticle(ignore -> wiring.register(config().getString("wired.name")));
			}
		}

		private String deployVerticle(Consumer<String> registrar) {
			String name = VerticleFactory.removePrefix(verticleName);
			log.debug("Deploying verticle {}", name);
			vertx.deployVerticle(
				name,
				new DeploymentOptions(vertx.getOrCreateContext().config()),
				ignore -> {
					vertx.undeploy(verticleName);
					registrar.accept(config().getString("wired.name"));
				}
			);
			return name;
		}
	}
}
