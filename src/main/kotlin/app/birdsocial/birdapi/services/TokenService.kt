package app.birdsocial.birdapi.services

import app.birdsocial.birdapi.config.EnvironmentData
import app.birdsocial.birdapi.exceptions.AuthException
import app.birdsocial.birdapi.helper.SentryHelper
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.Instant

@Service
class TokenService(
    val envData: EnvironmentData,
    val sentryHelper: SentryHelper,
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
            val algorithm = sentryHelper.span("env", "getData") {
                if (!refresh)
                    Algorithm.HMAC256(envData.getData("JWT_ACCESS_SECRET"))
                else
                    Algorithm.HMAC256(envData.getData("JWT_REFRESH_SECRET"))
            }

            val verifier: JWTVerifier = sentryHelper.span("compute", "createVerifier") {
                JWT.require(algorithm) // specify a specific claim validations
                    .withIssuer("api.birdsocial.app")
                    .build()
            }

            decodedJWT = sentryHelper.span("compute", "verify") { verifier.verify(token) }
            return decodedJWT
        } catch (exception: JWTVerificationException) {
            // Invalid signature/claims
            throw AuthException()
        }
    }

    fun createAccessToken(jwt_refresh: String): String {
        val uuid = getToken(jwt_refresh, true).audience[0]
        try {
            val algorithm = Algorithm.HMAC256(envData.getData("JWT_ACCESS_SECRET"))

            return JWT.create()
                .withIssuer("api.birdsocial.app")
                .withAudience(uuid)
                .withIssuedAt(Instant.now())
                .withExpiresAt(Instant.now().plusSeconds(900L))
                .withClaim("token-type", "access")
                .sign(algorithm)
        } catch (exception: JWTCreationException) {
            // Invalid Signing configuration / Couldn't convert Claims.
            throw AuthException()
        }
    }

    fun createRefreshToken(uuid: String): String {
        try {
            val algorithm = Algorithm.HMAC256(envData.getData("JWT_REFRESH_SECRET"))

            return JWT.create()
                .withIssuer("api.birdsocial.app")
                .withAudience(uuid.toString())
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