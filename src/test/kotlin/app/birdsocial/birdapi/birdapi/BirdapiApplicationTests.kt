package app.birdsocial.birdapi.birdapi

import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.ApplicationContext
import org.springframework.graphql.test.tester.HttpGraphQlTester
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.WebTestClient.Builder
import org.springframework.test.web.servlet.client.MockMvcWebTestClient
import org.springframework.web.context.WebApplicationContext


@SpringBootTest
class BirdapiApplicationTests(val applicationContext: ApplicationContext) {

//	@Test
//	fun createUser() {
//		val client: WebTestClient = MockMvcWebTestClient.bindToApplicationContext(applicationContext as WebApplicationContext)
//			.configureClient()
//			.baseUrl("/graphql")
//			.build()
//
////		val tester = HttpGraphQlTester.create(client)
//
//		val tester = HttpGraphQlTester.create(client).document(
//			"""
//			mutation {
//				createAccount(
//					auth: {
//						email:"example@example.com",
//						password:"Password1!"
//					}) {
//					user {
//						userId
//					}
//					access_token
//					refresh_token
//				}
//			}
//			""".trimIndent())
//
//		println("createUser() Test - ${tester.executeAndVerify()}")
//	}

}
