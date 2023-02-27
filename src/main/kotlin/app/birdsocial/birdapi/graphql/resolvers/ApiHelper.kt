package app.birdsocial.birdapi.graphql.resolvers

import app.birdsocial.birdapi.exceptions.ThrottleRequestException
import app.birdsocial.birdapi.services.TokenService
import io.github.bucket4j.local.LocalBucket
import org.springframework.stereotype.Component

@Component
class ApiHelper(
    val bucket: LocalBucket,

    val tokenService: TokenService,
) {
    final fun throttleRequest(numTokens: Long) {
        println("Tokens: ${bucket.availableTokens}")

        if (!bucket.tryConsume(numTokens))
            throw ThrottleRequestException()
    }
}