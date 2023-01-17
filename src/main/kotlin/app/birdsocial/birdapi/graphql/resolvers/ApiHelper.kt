package app.birdsocial.birdapi.graphql.resolvers

import app.birdsocial.birdapi.exceptions.AuthException
import app.birdsocial.birdapi.exceptions.ThrottleRequestException
import app.birdsocial.birdapi.helper.SentryHelper
import app.birdsocial.birdapi.services.TokenService
import io.github.bucket4j.local.LocalBucket
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
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