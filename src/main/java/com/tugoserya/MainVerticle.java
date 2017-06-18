package com.tugoserya;

import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableMap;
import com.tugoserya.model.Kid;
import com.tugoserya.services.AccountService;
import com.tugoserya.services.AccountServiceImpl;
import com.tugoserya.services.AdminService;
import com.tugoserya.services.AdminServiceImpl;
import com.tugoserya.utils.LocalDateDeserializer;
import com.tugoserya.utils.LocalDateSerializer;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
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

import java.time.LocalDate;

import static com.tugoserya.utils.Utils.currentUserName;
import static com.tugoserya.utils.Utils.forbid;
import static com.tugoserya.utils.Utils.ifInRole;
import static com.tugoserya.utils.Utils.toJson;
import static io.netty.handler.codec.http.HttpResponseStatus.FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.TEMPORARY_REDIRECT;
import static io.vertx.ext.auth.shiro.PropertiesProviderConstants.PROPERTIES_PROPS_PATH_FIELD;

public class MainVerticle extends AbstractVerticle {
	@Override
	public void start(Future<Void> fut) throws Exception {
		ShiroAuthOptions options = new ShiroAuthOptions().setConfig(new JsonObject(
			ImmutableMap.of(PROPERTIES_PROPS_PATH_FIELD, "classpath:vertx-users.properties")
		));
		SimpleModule module = new SimpleModule();
		module.addSerializer(LocalDate.class, new LocalDateSerializer());
		module.addDeserializer(LocalDate.class, new LocalDateDeserializer());
		Json.mapper.registerModule(module);
		Json.prettyMapper.registerModule(module);
		AdminService adminService = new AdminServiceImpl();
		AccountService accountService = new AccountServiceImpl();
		AuthProvider authProvider = ShiroAuth.create(vertx, options);
		Router router = Router.router(vertx);
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
		router.get("/api/accounts").handler(ifInRole("accounts:get").then(toJson(ctx -> adminService.getAccounts())));
		router.delete("/api/accounts").handler(ifInRole("accounts:delete").then(toJson(ctx -> {
				String accountId = ctx.request().getParam("id");
				if (!currentUserName(ctx).equals(accountId)) {
					return adminService.removeAccount(accountId).map(true);
				} else {
					return forbid();
				}
			})
		));
		router.get("/api/kids").handler(ifInRole("kids:get").then(toJson(ctx ->
			accountService.getKids(currentUserName(ctx)))));
		router.put("/api/kids").handler(ifInRole("kids:put").then(toJson(ctx -> {
				Kid kid = Json.decodeValue(ctx.getBodyAsString(), Kid.class);
				if (currentUserName(ctx).equals(kid.getAccountId())) {
					return accountService.putKid(kid).map(true);
				} else {
					return forbid();
				}
			}
		)));
		router.route().handler(StaticHandler.create());
		vertx.createHttpServer().requestHandler(router::accept).listen(8080);
	}

}
