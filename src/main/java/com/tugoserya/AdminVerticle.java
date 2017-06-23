package com.tugoserya;

import com.tugoserya.services.AdminService;
import com.tugoserya.utils.Dependencies;
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

	@Autowired
	private Router router;
	@Autowired
	private AdminService adminService;
	@Autowired
	private Dependencies dependencies;

	@Override
	public void start() throws Exception {
		router.get("/api/accounts")
			.handler(ifInRole("accounts:get").then(toJson(ctx -> adminService.getAccounts())));
		router.delete("/api/accounts").handler(ifInRole("accounts:delete").then(toJson(ctx -> {
				String accountId = ctx.request().getParam("id");
				if (!currentUserName(ctx).equals(accountId)) {
					return adminService.removeAccount(accountId).map(true);
				} else {
					return forbid();
				}
			})
		));
	}
}
