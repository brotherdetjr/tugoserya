package com.tugoserya.services;


import com.tugoserya.model.Kid;
import io.vertx.core.Future;

import java.util.List;

public interface AccountService {
	Future<Void> putKid(Kid kid);
	Future<List<Kid>> getKids(String accountId);
}
