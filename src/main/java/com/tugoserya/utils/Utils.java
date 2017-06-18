package com.tugoserya.utils;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;

import java.time.format.DateTimeFormatter;
import java.util.function.Function;

import static com.google.common.base.Throwables.getStackTraceAsString;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.vertx.core.Future.future;
import static io.vertx.core.Future.succeededFuture;
import static java.time.format.DateTimeFormatter.ofPattern;
import static org.slf4j.LoggerFactory.getLogger;

public class Utils {
	public static final DateTimeFormatter YYYYMMDD = ofPattern("yyyyMMdd");
	public static final String DEPLOYMENT_EVENTS = "deployment.events";

	private static final Logger log = getLogger(Utils.class);

	private Utils() {
		throw new AssertionError();
	}

	public static IfInRole ifInRole(String authority) {
		return new IfInRole(authority);
	}

	public static Future<RoutingContext> checkRole(RoutingContext ctx, String authority) {
		Future<RoutingContext> future = future();
		try {
			ctx.user().isAuthorised(authority, authorized -> {
				if (authorized.succeeded()) {
					if (authorized.result()) {
						future.complete(ctx);
					} else {
						future.fail(new HttpResponseException(FORBIDDEN));
					}
				} else {
					future.fail(new HttpResponseException(INTERNAL_SERVER_ERROR, authorized.cause()));
				}
			});
		} catch (Exception e) {
			future.fail(new HttpResponseException(INTERNAL_SERVER_ERROR, e));
		}
		return future;
	}

	public static <T> Function<Throwable, T> fail(RoutingContext ctx) {
		return throwable -> {
			log.error(getStackTraceAsString(throwable));
			int status;
			if (throwable instanceof HttpResponseException) {
				status = ((HttpResponseException) throwable).getStatus().code();
			} else {
				status = INTERNAL_SERVER_ERROR.code();
			}
			ctx.fail(status);
			return null;
		};
	}

	public static <T> Function<T, Future<Void>> sendJson(RoutingContext ctx) {
		return value -> {
			ctx.response()
				.putHeader("content-type", "application/json; charset=utf-8")
				.end(Json.encode(value));
			return succeededFuture();
		};
	}

	public static <T> Handler<RoutingContext> toJson(Function<RoutingContext, Future<T>> func) {
		return ctx -> succeededFuture(ctx).compose(func).compose(sendJson(ctx)).otherwise(fail(ctx));
	}

	public static class IfInRole {
		private final String role;

		public IfInRole(String role) {
			this.role = role;
		}

		public Handler<RoutingContext> then(Handler<RoutingContext> handler) {
			return ctx -> checkRole(ctx, role).map(c -> {handler.handle(c); return c; });
		}

	}

	public static <T> Future<T> forbid() {
		throw new HttpResponseException(FORBIDDEN);
	}

	public static class HttpResponseException extends RuntimeException {
		private final HttpResponseStatus status;

		public HttpResponseException(HttpResponseStatus status) {
			this.status = status;
		}

		public HttpResponseException(HttpResponseStatus status, Throwable throwable) {
			super(throwable);
			this.status = status;
		}

		public HttpResponseStatus getStatus() {
			return status;
		}
	}

	public static String currentUserName(RoutingContext ctx) {
		return ctx.user().principal().getString("username");
	}
}
