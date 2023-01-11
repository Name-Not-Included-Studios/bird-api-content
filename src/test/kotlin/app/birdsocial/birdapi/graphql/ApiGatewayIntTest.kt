package app.birdsocial.birdapi.graphql

import app.birdsocial.birdapi.graphql.types.LoginResponse
import app.birdsocial.birdapi.graphql.types.User
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.graphql.test.tester.GraphQlTester
import kotlin.RuntimeException

//@Import(
//    HttpServletRequest::class,
//    UserResolver::class
//)
//@GraphQlTest(UserResolver::class)
@SpringBootTest
internal class ApiGatewayIntTest {
    @Autowired
    val graphQlTester: GraphQlTester? = null

//    @Test
    fun testCreateAccount() {
        // language=GraphQL
        val user_create = """
            mutation {
                createAccount(
                    auth: {
                        email:"example@example.com",
                        password:"Password1!"
                    }) {
                    user {
                        userId
                    }
                    access_token
                    refresh_token
                }
            }
        """.trimIndent()

//        val createEntity = graphQlTester?.document(user_create)
//            ?.execute()
//            ?.path("createAccount")
//            ?.entity(LoginResponse::class.java)
//            .satisfies<LoginResponse> { res -> {
//                assert()
//            }}
//            ?: throw RuntimeException("LoginResponse is null")

        // language=GraphQL
        val user_update = """
            mutation {
                updateUser(
                    user: {
                        username: "testname"
                        displayName: "Test Name"
                        bio: "Test Bio"
                        avatarUrl: ""
                        websiteUrl: ""
                    }
                ) {
                    userId
                    username
                    displayName
                    bio
                }
            }
        """.trimIndent()

//        if (createResponse.user != User(
//                createResponse.user.userId,
//                "testname",
//                "Test Name",
//                "Test Bio",
//                "",
//                "",
//                -1,
//                -1,
//                -1
//            )) throw RuntimeException("User is not equal")
    }
}