package com.tugoserya

import io.vertx.core.Vertx
import io.vertx.ext.unit.TestContext
import io.vertx.ext.unit.junit.VertxUnitRunner
import org.junit.After
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith

@Ignore
@RunWith(VertxUnitRunner)
class MainVerticleTest {

	def vertx

	@Before
	void setup(TestContext context) {
		vertx = Vertx.vertx()
		vertx.deployVerticle MainVerticle.name, context.asyncAssertSuccess()
	}

	@After
	void cleanup(TestContext context) {
		vertx.close context.asyncAssertSuccess()
	}

	@Test
	void 'http response contains Hello'(TestContext context) {
		def async = context.async()

		vertx.createHttpClient().getNow 8080, 'localhost', '/', { response ->
			response.handler { body ->
				context.assertTrue body.toString().contains('Hello')
				async.complete()
			}
		}
	}

}