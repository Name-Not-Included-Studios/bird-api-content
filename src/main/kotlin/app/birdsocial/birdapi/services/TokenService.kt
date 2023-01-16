package app.birdsocial.birdapi.services

import app.birdsocial.birdapi.exceptions.AuthException
import app.birdsocial.birdapi.helper.SentryHelper
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class TokenService(
    val env: Environment,
    val sentry: SentryHelper,
    ) {
    // TODO Fix str ct

    fun checkToken(token: String, refresh: Boolean): Boolean {
        return try {
            getToken(token, refresh)
            true
        } catch (exception: Exception) {
            false
        }
    }

    fun getToken(token: String, refresh: Boolean): DecodedJWT {
        val decodedJWT: DecodedJWT
        try {
            val algorithm = sentry.span("env", "getData") {
                if (!refresh)
                    Algorithm.HMAC256(env["JWT_ACCESS_SECRET"])
                else
                    Algorithm.HMAC256(env["JWT_REFRESH_SECRET"])
            }

            val verifier: JWTVerifier = sentry.span("compute", "createVerifier") {
                JWT.require(algorithm) // specify a specific claim validations
                    .withIssuer("api.birdsocial.app")
                    .build()
            }

            decodedJWT = sentry.span("compute", "verify") { verifier.verify(token) }
            return decodedJWT
        } catch (exception: JWTVerificationException) {
            // Invalid signature/claims
            throw AuthException()
        }
    }

    fun createAccessToken(jwt_refresh: String): String {
        val uuid = getToken(jwt_refresh, true).audience[0]
        try {
            val algorithm = Algorithm.HMAC256(env["JWT_ACCESS_SECRET"])

            return JWT.create()
                .withIssuer("api.birdsocial.app")
                .withAudience(uuid)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusSeconds(900L))
                .withClaim("token-type", "access")
                .withClaim("session-id", UUID.randomUUID().toString())
                .sign(algorithm)
        } catch (exception: JWTCreationException) {
            // Invalid Signing configuration / Couldn't convert Claims.
            throw AuthException()
        }
    }

    fun createRefreshToken(uuid: String): String {
        try {
            val algorithm = Algorithm.HMAC256(env["JWT_REFRESH_SECRET"])

            return JWT.create()
                .withIssuer("api.birdsocial.app")
                .withAudience(uuid)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plus(Duration.ofDays(90)))
                .withClaim("token-type", "refresh")
                .sign(algorithm)
        } catch (exception: JWTCreationException) {
            // Invalid Signing configuration / Couldn't convert Claims.
            throw AuthException()
        }
    }
}