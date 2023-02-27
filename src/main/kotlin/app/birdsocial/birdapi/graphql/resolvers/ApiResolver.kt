package app.birdsocial.birdapi.graphql.resolvers

import app.birdsocial.birdapi.graphql.types.ApiStatus
import app.birdsocial.birdapi.services.SentryHelper
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class ApiResolver(
    val api: ApiHelper,
    val sentry: SentryHelper,
    val env: Environment,
) {
    @QueryMapping
    fun apiStatus(): ApiStatus { // TODO Bump Version (optional)
        return ApiStatus(env["API_VERSION"] ?: "UNKNOWN")
    }

    @QueryMapping
    fun apiVersion(): String = sentry.captureTransaction {
        return env["API_VERSION"] ?: "UNKNOWN"
    }
}
