package com.tugoserya;

import com.tugoserya.model.Kid;
import com.tugoserya.services.AccountService;
import com.tugoserya.utils.Wiring;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.Json;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import static com.tugoserya.utils.Utils.currentUserName;
import static com.tugoserya.utils.Utils.forbid;
import static com.tugoserya.utils.Utils.ifInRole;
import static com.tugoserya.utils.Utils.toJson;
import static org.slf4j.LoggerFactory.getLogger;

public class UserVerticle extends AbstractVerticle {

	private static final Logger log = getLogger(UserVerticle.class);

	@Autowired
	private Router router;
	@Autowired
	private AccountService accountService;
	@Autowired
	private Wiring wiring;

	@Override
	public void start() throws Exception {
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
	}
}
