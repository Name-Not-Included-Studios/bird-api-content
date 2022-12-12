package app.birdsocial.birdapi.middleware

import app.birdsocial.birdapi.EnvironmentData
import app.birdsocial.birdapi.graphql.exceptions.AuthException
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import java.time.Instant


// TODO Fix str ct
//class UserAuthenticator {
//    companion object {
fun checkAccessToken(token: String): Boolean {
    val decodedJWT: DecodedJWT
    try {
        val algorithm = Algorithm.HMAC256(EnvironmentData.getData("JWT_ACCESS_SECRET"))
        val verifier: JWTVerifier = JWT.require(algorithm) // specify an specific claim validations
            .withIssuer("birdapi")
            .build()
        decodedJWT = verifier.verify(token)
        return true
    } catch (exception: JWTVerificationException) {
        // Invalid signature/claims
        throw AuthException()
    }
    // (EnvironmentData.getData("JWT_SESSION_SECRET") == token) // JWT_REFRESH_SECRET
}

fun createSessionToken(jwt_refresh: String): String {
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

//        val jwt: String = JWT.create()
//            .withHeader(headerMap)
//            .withClaim("string-claim", "string-value")
//            .withClaim("number-claim", 42)
//            .withClaim("bool-claim", true)
//            .withClaim("datetime-claim", Instant.now())
//            .sign(algorithm)
}
//    }
//}