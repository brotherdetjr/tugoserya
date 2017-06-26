package com.tugoserya

import com.tugoserya.model.Account
import com.tugoserya.services.AdminService
import io.vertx.ext.web.Router
import spock.lang.Specification
import spock.util.concurrent.BlockingVariables

import static io.vertx.core.Future.succeededFuture
import static io.vertx.core.Vertx.vertx

class AdminVerticleTest extends Specification {

	def 'does my day'() {
		given:
		def vertx = vertx()
		def router = Router.router(vertx)
		def adminService = Mock(AdminService) {
			getAccounts() >> succeededFuture([new Account('disa'), new Account('max')])
		}
		def verticle = new AdminVerticle(router: router, adminService: adminService)
		def barrier = new BlockingVariables()
		vertx.createHttpServer().requestHandler(router.&accept).listen 0, {
			barrier.port = it.result().actualPort()
		}
		vertx.deployVerticle verticle, {
			barrier.deployed = true
		}
		barrier.port
		barrier.deployed
		expect:
		barrier.port != 0
		2 + 2 == 4
	}
}