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

	private Router router;
	private AccountService accountService;

	@Override
	public void start() throws Exception {
		Router kidsApi = Router.router(vertx);
		kidsApi.get("/kids").handler(ifInRole("kids:get").then(toJson(ctx ->
			accountService.getKids(currentUserName(ctx)))));
		kidsApi.put("/kids").handler(ifInRole("kids:put").then(toJson(ctx -> {
				Kid kid = Json.decodeValue(ctx.getBodyAsString(), Kid.class);
				if (currentUserName(ctx).equals(kid.getAccountId())) {
					return accountService.putKid(kid).map(true);
				} else {
					return forbid();
				}
			}
		)));
		router.mountSubRouter("/api", kidsApi);
	}

	public void setRouter(Router router) {
		this.router = router;
	}

	public void setAccountService(AccountService accountService) {
		this.accountService = accountService;
	}
}
