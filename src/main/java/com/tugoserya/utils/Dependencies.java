package com.tugoserya.utils;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.eventbus.MessageCodec;
import io.vertx.core.eventbus.MessageConsumer;

import java.util.function.Supplier;

import static com.tugoserya.utils.Dependencies.MsgType.CHECK;
import static com.tugoserya.utils.Dependencies.MsgType.NOTIFY;
import static com.tugoserya.utils.Dependencies.MsgType.REPLY;
import static com.tugoserya.utils.Dependencies.MsgType.fromInt;
import static io.netty.util.CharsetUtil.US_ASCII;
import static io.vertx.core.Future.future;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

public class Dependencies {

	private final EventBus eventBus;
	private final String address;
	private final boolean local;

	public Dependencies(EventBus eventBus, String address, boolean local) {
		this.eventBus = eventBus;
		this.address = address;
		this.local = local;
		eventBus.registerDefaultCodec(Msg.class, new MsgCodec());
	}

	public void waitFor(Supplier<String> everybodyIsHere, String... names) {
		stream(names).forEach(name -> publish(Msg.check(name)));
		CompositeFuture.all(
			stream(names)
				.map(this::waitFor)
				.collect(toList())
		).compose(f -> {
			String myName = everybodyIsHere.get();
			handler(m -> {
				Msg msg = m.body();
				MsgType type = msg.getType();
				if ((myName.equals(msg.getName())) && (type == CHECK)) {
					publish(Msg.reply(myName));
				}
			});
			publish(Msg.notify(myName));
			return null;
		});
	}

	public void notify(String name) {
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
