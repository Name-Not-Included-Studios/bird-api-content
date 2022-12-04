package app.birdsocial.birdapi

import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class UserController {

    @QueryMapping
    fun getUser(): User {
        var userOne = User("fdfd", "df");

        return userOne;
    }
}