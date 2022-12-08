package app.birdsocial.birdapi.api

import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.graphql.data.method.annotation.SchemaMapping
import org.springframework.stereotype.Controller

//TODO Bump Version (optional)
@Controller
class ApiResolver {
    @QueryMapping
    fun apiStatus(): ApiStatus {
        return ApiStatus("0.1-SNAPSHOT")
    }
}