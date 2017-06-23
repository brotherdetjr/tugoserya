package com.tugoserya.utils;

import io.vertx.core.CompositeFuture;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.MessageConsumer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.copyOf;
import static com.tugoserya.utils.Dependencies.MsgType.CHECK;
import static com.tugoserya.utils.Dependencies.MsgType.NOTIFY;
import static com.tugoserya.utils.Dependencies.MsgType.REPLY;
import static com.tugoserya.utils.Dependencies.MsgType.fromInt;
import static io.netty.util.CharsetUtil.US_ASCII;
import static io.vertx.core.Future.future;
import static io.vertx.core.Future.succeededFuture;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;
import static org.slf4j.LoggerFactory.getLogger;

public class Dependencies {

	private static final Logger log = getLogger(Dependencies.class);

	private final EventBus eventBus;
	private final String address;
	private final boolean local;

	public Dependencies(EventBus eventBus, String address, boolean local) {
		this.eventBus = eventBus;
		this.address = address;
		this.local = local;
		eventBus.registerDefaultCodec(Msg.class, new MsgCodec());
	}

	public Dependencies(EventBus eventBus, String address) {
		this(eventBus, address, true);
	}

	public static DeploymentOptions toOptions(String wiredName, List<String> depList) {
		JsonObject config = new JsonObject().put("wired.deps", new JsonArray(depList)).put("wired.name", wiredName);
		return new DeploymentOptions().setConfig(config);
	}

	public static DeploymentOptions toOptions(String wiredName) {
		return toOptions(wiredName, emptyList());
	}

	public class Having {
		private final Set<String> names;

		public Having(Set<String> names) {
			this.names = copyOf(names);
		}

		public Future<Void> register(String name) {
			names.forEach(n -> publish(Msg.check(n)));
			return CompositeFuture.all(
				names.stream()
					.map(Dependencies.this::waitFor)
					.collect(toList())
			).compose(f -> {
				log.debug("Located all required dependencies {} for {}",
					f.list().stream().map(m -> ((Msg) m).getName()).collect(toList()), name);
				handler(m -> {
					Msg msg = m.body();
					MsgType type = msg.getType();
					if ((name.equals(msg.getName())) && (type == CHECK)) {
						publish(Msg.reply(name));
					}
				});
				publish(Msg.notify(name));
				return succeededFuture();
			});
		}
	}

	public Having having(String ... names) {
		return new Having(copyOf(names));
	}

	public Having having(Collection<String> names) {
		return new Having(copyOf(names));
	}

	public void register(String name) {
		log.debug("Registering dependency {}", name);
		publish(Msg.notify(name));
	}

	private Future<Msg> waitFor(String name) {
		Future<Msg> future = future();
		handler(m -> {
			Msg msg = m.body();
			MsgType type = msg.getType();
			if ((name.equals(msg.getName())) && (type == REPLY || type == NOTIFY)) {
				future.complete(msg);
			}
		});
		return future;
	}

	private MessageConsumer<Msg> consumer() {
		return local ? eventBus.localConsumer(address) : eventBus.consumer(address);
	}

	private void handler(Handler<Message<Msg>> handler) {
		consumer().handler(handler);
	}

	private void publish(Msg msg) {
		eventBus.publish(address, msg);
	}

	public enum MsgType {
		CHECK(0), REPLY(1), NOTIFY(2);

		private final int code;

		MsgType(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}

		public static MsgType fromInt(int i) {
			if (CHECK.getCode() == i) {
				return CHECK;
			} else if (REPLY.getCode() == i) {
				return REPLY;
			} else if (NOTIFY.getCode() == i) {
				return NOTIFY;
			} else {
				throw new IllegalArgumentException();
			}
		}
	}

	public static class Msg {
		private final MsgType type;
		private final String name;

		public Msg(MsgType type, String name) {
			this.type = type;
			this.name = name;
		}

		public MsgType getType() {
			return type;
		}

		public String getName() {
			return name;
		}

		public static Msg check(String name) {
			return new Msg(CHECK, name);
		}

		public static Msg reply(String name) {
			return new Msg(REPLY, name);
		}

		public static Msg notify(String name) {
			return new Msg(NOTIFY, name);
		}
	}

	public static class MsgCodec implements MessageCodec<Msg, Msg> {
		@Override
		public void encodeToWire(Buffer buffer, Msg msg) {
			buffer
				.appendInt(msg.getType().getCode())
				.appendInt(msg.getName().length())
				.appendString(msg.getName(), US_ASCII.name());
		}

		@Override
		public Msg decodeFromWire(int pos, Buffer buffer) {
			return new Msg(
				fromInt(buffer.getInt(pos)),
				buffer.getString(pos + 8, pos + 8 + buffer.getInt(pos + 4), US_ASCII.name())
			);
		}

		@Override
		public Msg transform(Msg msg) {
			return msg;
		}

		@Override
		public String name() {
			return Msg.class.getSimpleName().toLowerCase();
		}

		@Override
		public byte systemCodecID() {
			return -1;
		}
	}

}
