package app.birdsocial.birdapi.graphql

import app.birdsocial.birdapi.graphql.resolvers.ApiGateway
import app.birdsocial.birdapi.graphql.types.AuthInput
import app.birdsocial.birdapi.graphql.types.User
import app.birdsocial.birdapi.graphql.types.UserInput
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.RuntimeException

//@GraphQlTest(UserResolver::class)
@SpringBootTest
internal class ApiGatewayIntTest2(@Autowired val apiGateway: ApiGateway) {

//    @Test
    fun testCreateAccount() {
        val createResponse = apiGateway.createAccount(
            AuthInput("example@example.com", "Password1!")
        )
        println("createAccount() Test Passed")

//        apiGateway.access = createResponse.access_token

        val updateResponse = apiGateway.updateUser(
            UserInput(
                "testname",
                "Test Name",
                "Test Bio",
                "",
                "",
            )
        )

        if (createResponse.user != User(
                createResponse.user.userId,
                "testname",
                "Test Name",
                "Test Bio",
                "",
                "",
                -1,
                -1,
                -1
            )) throw RuntimeException("User is not equal")

        val getResponse = apiGateway.getMe()

        if (createResponse.user != User(
                createResponse.user.userId,
                "testname",
                "Test Name",
                "Test Bio",
                "",
                "",
                -1,
                -1,
                -1
            )) throw RuntimeException("User is not equal")
    }
}