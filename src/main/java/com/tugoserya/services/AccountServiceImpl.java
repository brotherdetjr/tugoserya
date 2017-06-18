package com.tugoserya.services;

import com.google.common.collect.ImmutableList;
import com.tugoserya.model.Kid;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import org.slf4j.Logger;

import java.time.LocalDate;
import java.util.List;

import static io.vertx.core.Future.succeededFuture;
import static java.util.Collections.emptyList;
import static org.slf4j.LoggerFactory.getLogger;

public class AccountServiceImpl implements AccountService {

	private static final Logger log = getLogger(AccountServiceImpl.class);

	@Override
	public Future<List<Kid>> getKids(String accountId) {
		if ("disa".equals(accountId)) {
			return succeededFuture(ImmutableList.of(
				new Kid("Polya", LocalDate.parse("2013-09-06"), "disa"),
				new Kid("Pavlik", LocalDate.parse("2016-08-14"), "disa")
			));
		} else {
			return succeededFuture(emptyList());
		}
	}

	@Override
	public Future<Void> putKid(Kid kid) {
		log.debug("Putting kid {}", Json.encode(kid));
		return succeededFuture();
	}
}
