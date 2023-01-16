package app.birdsocial.birdapi.graphql.api

import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class ApiResolver {
    @QueryMapping
    fun apiStatus(): ApiStatus { // TODO Bump Version (optional)
        return ApiStatus("0.1-SNAPSHOT")
    }
}
