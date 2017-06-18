package com.tugoserya.services;

import com.google.common.collect.ImmutableList;
import com.tugoserya.model.Account;
import io.vertx.core.Future;
import org.slf4j.Logger;

import java.util.List;

import static io.vertx.core.Future.succeededFuture;
import static org.slf4j.LoggerFactory.getLogger;

public class AdminServiceImpl implements AdminService {

	private static final Logger log = getLogger(AdminServiceImpl.class);

	@Override
	public Future<List<Account>> getAccounts() {
		return succeededFuture(
			ImmutableList.of(
				new Account("disa"),
				new Account("max"),
				new Account("ovu1"),
				new Account("ovu2")
			)
		);
	}

	@Override
	public Future<Void> removeAccount(String id) {
		log.debug("Removing account {}", id);
		return succeededFuture();
	}
}
