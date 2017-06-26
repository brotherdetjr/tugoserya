package com.tugoserya;

import com.tugoserya.services.AdminService;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import static com.tugoserya.utils.Utils.currentUserName;
import static com.tugoserya.utils.Utils.forbid;
import static com.tugoserya.utils.Utils.ifInRole;
import static com.tugoserya.utils.Utils.toJson;
import static org.slf4j.LoggerFactory.getLogger;

public class AdminVerticle extends AbstractVerticle {
	private static final Logger log = getLogger(AdminVerticle.class);

	private Router router;
	private AdminService adminService;

	@Override
	public void start() throws Exception {
		Router accountsApi = Router.router(vertx);
		accountsApi.get("/accounts")
			.handler(ifInRole("accounts:get").then(toJson(ctx -> adminService.getAccounts())));
		accountsApi.delete("/accounts").handler(ifInRole("accounts:delete").then(toJson(ctx -> {
				String accountId = ctx.request().getParam("id");
				if (!currentUserName(ctx).equals(accountId)) {
					return adminService.removeAccount(accountId).map(true);
				} else {
					return forbid();
				}
			})
		));
		router.mountSubRouter("/api", accountsApi);
	}

	public void setRouter(Router router) {
		this.router = router;
	}

	public void setAdminService(AdminService adminService) {
		this.adminService = adminService;
	}
}
