package com.tugoserya;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableMap;
import com.tugoserya.utils.Dependencies;
import com.tugoserya.utils.LocalDateDeserializer;
import com.tugoserya.utils.LocalDateSerializer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.AuthProvider;
import io.vertx.ext.auth.shiro.ShiroAuth;
import io.vertx.ext.auth.shiro.ShiroAuthOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.FormLoginHandler;
import io.vertx.ext.web.handler.RedirectAuthHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.handler.UserSessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;

import static io.netty.handler.codec.http.HttpResponseStatus.FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.TEMPORARY_REDIRECT;
import static io.vertx.ext.auth.shiro.PropertiesProviderConstants.PROPERTIES_PROPS_PATH_FIELD;
import static org.slf4j.LoggerFactory.getLogger;

public class MainVerticle extends AbstractVerticle {

	private static final Logger log = getLogger(MainVerticle.class);

	@Autowired
	private Router router;
	@Autowired
	private Dependencies dependencies;

	@Override
	public void start() throws Exception {
		ShiroAuthOptions options = new ShiroAuthOptions().setConfig(new JsonObject(
			ImmutableMap.of(PROPERTIES_PROPS_PATH_FIELD, "classpath:vertx-users.properties")
		));
		SimpleModule module = new SimpleModule();
		module.addSerializer(LocalDate.class, new LocalDateSerializer());
		module.addDeserializer(LocalDate.class, new LocalDateDeserializer());
		Json.mapper.registerModule(module);
		Json.prettyMapper.registerModule(module);
		AuthProvider authProvider = ShiroAuth.create(vertx, options);
		router.route().handler(CookieHandler.create());
		router.route().handler(BodyHandler.create());
		router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
		router.route().handler(UserSessionHandler.create(authProvider));
		router.route("/").handler(ctx ->
			ctx.response().putHeader("location", "/login.html").setStatusCode(TEMPORARY_REDIRECT.code()).end()
		);
		router.route("/api/*").handler(RedirectAuthHandler.create(authProvider, "/login.html"));
		router.route("/login").handler(FormLoginHandler.create(authProvider));
		router.route("/logout").handler(ctx -> {
			ctx.clearUser();
			ctx.response().putHeader("location", "/login.html").setStatusCode(FOUND.code()).end();
		});
		router.routeWithRegex("^/(?!api/).*$").handler(StaticHandler.create());
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
		dependencies.register("main");
	}

}
