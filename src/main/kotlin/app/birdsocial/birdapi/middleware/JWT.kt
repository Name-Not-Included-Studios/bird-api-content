package app.birdsocial.birdapi.middleware

import app.birdsocial.birdapi.EnvironmentData
import app.birdsocial.birdapi.graphql.exceptions.AuthException
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import java.time.Duration
import java.time.Instant
import java.time.temporal.TemporalAmount


// TODO Fix str ct
//class UserAuthenticator {
//    companion object {
fun checkToken(token: String, refresh: Boolean): Boolean {
    val decodedJWT: DecodedJWT
    try {
        val algorithm = if (!refresh)
            Algorithm.HMAC256(EnvironmentData.getData("JWT_ACCESS_SECRET"))
        else
            Algorithm.HMAC256(EnvironmentData.getData("JWT_REFRESH_SECRET"))

        val verifier: JWTVerifier = JWT.require(algorithm) // specify a specific claim validations
            .withIssuer("api.birdsocial.app")
            .build()
        decodedJWT = verifier.verify(token)
        return true
    } catch (exception: JWTVerificationException) {
        // Invalid signature/claims
        //throw AuthException()
        return false
    }
}

fun getToken(token: String, refresh: Boolean): DecodedJWT {
    val decodedJWT: DecodedJWT
    try {
        val algorithm = if (!refresh)
            Algorithm.HMAC256(EnvironmentData.getData("JWT_ACCESS_SECRET"))
        else
            Algorithm.HMAC256(EnvironmentData.getData("JWT_REFRESH_SECRET"))

        val verifier: JWTVerifier = JWT.require(algorithm) // specify a specific claim validations
            .withIssuer("api.birdsocial.app")
            .build()
        decodedJWT = verifier.verify(token)
        return decodedJWT
    } catch (exception: JWTVerificationException) {
        // Invalid signature/claims
        throw AuthException()
    }
}

fun createAccessToken(jwt_refresh: String): String {
    try {
        val algorithm = Algorithm.HMAC256(EnvironmentData.getData("JWT_ACCESS_SECRET"))

        return JWT.create()
            .withIssuer("api.birdsocial.app")
            .withAudience("userId")
            .withIssuedAt(Instant.now())
            .withExpiresAt(Instant.now().plusSeconds(900L))
            .withClaim("string-claim", "string-value")
            .sign(algorithm)
    } catch (exception: JWTCreationException) {
        // Invalid Signing configuration / Couldn't convert Claims.
        throw AuthException()
    }
}

fun createRefreshToken(): String {
    try {
        val algorithm = Algorithm.HMAC256(EnvironmentData.getData("JWT_REFRESH_SECRET"))

        return JWT.create()
            .withIssuer("api.birdsocial.app")
            .withAudience("userId")
            .withIssuedAt(Instant.now())
            .withExpiresAt(Instant.now().plus(Duration.ofDays(90)))
            .withClaim("string-claim", "string-value")
            .sign(algorithm)
    } catch (exception: JWTCreationException) {
        // Invalid Signing configuration / Couldn't convert Claims.
        throw AuthException()
    }
}

//        val jwt: String = JWT.create()
//            .withHeader(headerMap)
//            .withClaim("string-claim", "string-value")
//            .withClaim("number-claim", 42)
//            .withClaim("bool-claim", true)
//            .withClaim("datetime-claim", Instant.now())
//            .sign(algorithm)
//    }

// (EnvironmentData.getData("JWT_SESSION_SECRET") == token) // JWT_REFRESH_SECRET