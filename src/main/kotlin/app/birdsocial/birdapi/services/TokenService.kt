package app.birdsocial.birdapi.services

import app.birdsocial.birdapi.exceptions.AuthException
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
class TokenService(
    val env: Environment,
    val sentry: SentryHelper,
    ) {
    final fun authorize(request: HttpServletRequest): String {
        // Get Authorization Header
        val access = request.getHeader(HttpHeaders.AUTHORIZATION) ?: throw AuthException()
        // Get User ID from refresh token (also checks if signature is valid)
        return sentry.span("tkn-srv", "getToken") { getToken(access, false).audience[0] }
    }

    // TODO Fix str ct

    fun checkToken(token: String, refresh: Boolean): Boolean {
        return try {
            getToken(token, refresh)
            true
        } catch (exception: Exception) {
            false
        }
    }

    private fun cleanToken(token: String): String {
        return if (token.startsWith("Bearer ")) token.substring(7) else token
    }

    fun getToken(dirtyToken: String, refresh: Boolean): DecodedJWT {
        val token = cleanToken(dirtyToken)

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

    fun createAccessToken(jwt_refresh: String): Pair<String, String> {
        val uuid = getToken(jwt_refresh, true).audience[0]
        try {
            val algorithm = Algorithm.HMAC256(env["JWT_ACCESS_SECRET"])

            val expiry = Instant.now().plusSeconds(900L)

            return Pair(JWT.create()
                .withIssuer("api.birdsocial.app")
                .withAudience(uuid)
                .withIssuedAt(Instant.now())
                .withExpiresAt(expiry)
                .withClaim("token-type", "access")
                .withClaim("session-id", UUID.randomUUID().toString())
                .sign(algorithm),
                expiry.toString()
            )
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