package com.tugoserya.services;

import com.tugoserya.model.Account;
import io.vertx.core.Future;

import java.util.List;

public interface AdminService {
	Future<List<Account>> getAccounts();
	Future<Void> removeAccount(String id);
}
