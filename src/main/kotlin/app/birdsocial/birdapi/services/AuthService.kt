package app.birdsocial.birdapi.services

import app.birdsocial.birdapi.exceptions.AuthException
import app.birdsocial.birdapi.exceptions.BirdException
import app.birdsocial.birdapi.exceptions.ResourceNotFoundException
import app.birdsocial.birdapi.graphql.types.AuthInput
import app.birdsocial.birdapi.graphql.types.LoginResponse
import app.birdsocial.birdapi.helper.SentryHelper
import app.birdsocial.birdapi.neo4j.repo.Neo4jRepository
import app.birdsocial.birdapi.neo4j.repo.UserRepository
import app.birdsocial.birdapi.neo4j.schemas.UserNode
import org.joda.time.DateTime
import org.mindrot.jbcrypt.BCrypt
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDateTime

@Service
class AuthService(
    val sentry: SentryHelper,
    val env: Environment,
    val tokenService: TokenService,

    val neo4jRepository: Neo4jRepository,
    val userRepository: UserRepository
) {

    fun login(userLogin: AuthInput): LoginResponse { // TODO - Change from LoginResponse to something else as LoginResponse is a GraphQL type
        try {
            // Get user from database with same email
            val userNode = neo4jRepository.findOneByParam<UserNode>("User", "email", userLogin.email)

            // Check password matches
            if (!sentry.span("compute", "checkpw") { BCrypt.checkpw(userLogin.password, userNode.password) })
                throw AuthException()

            // Officially Proven Identity Here
            userNode.lastLogin = Instant.now()

            // If stored refresh token is not valid (e.g. expired) then generate a new refresh token
//        if (!sentry.span("tokenService", "checkToken") { tokenService.checkToken(userNode.refreshToken, true) })
//            userNode.refreshToken = sentry.span("tkn-srv", "createRefreshToken") { tokenService.createRefreshToken(userNode.id) }

            sentry.span("neo4j", "save") { userRepository.save(userNode) }

            val tokenData = tokenService.createAccessToken(userNode.refreshToken)
            return LoginResponse(
                userNode.toUser(),
                tokenData.first,
                userNode.refreshToken,
                tokenData.second
            )
        } catch (ex: Exception) {
            throw AuthException()
        }
    }

    fun updatePassword(oldPassword: String, newPassword: String, access: String): LoginResponse {
        // (1) Get UserID from token
        // (2) Get Email from database using UserID
        // (3) Check Old Password
        // (4) Update New Password

        // (1) Get UserID from token
        val userId = sentry.span("tkn-srv", "getToken") { tokenService.getToken(access, false).audience[0] }

        // (2) Get Email from database using UserID
        val userNode = neo4jRepository.findOneById<UserNode>("User", userId)

        // (3) Check Old Password
        if (!sentry.span("compute", "checkpw") { BCrypt.checkpw(oldPassword, userNode.password) })
            throw AuthException()

        // If stored refresh token is not valid (e.g. expired) then generate a new refresh token
//        if (!sentry.span("tokenService", "checkToken") { tokenService.checkToken(userNode.refreshToken, true) })
//            userNode.refreshToken = sentry.span("tkn-srv", "createRefreshToken") { tokenService.createRefreshToken(userId) }

        // Officially Proven Identity Here

        // (4) Update New Password
        userNode.lastLogin = Instant.now()
        userNode.password = sentry.span("compute", "hashpw") {
            BCrypt.hashpw(
                newPassword,
                BCrypt.gensalt(env["BCRYPT_LOG_ROUNDS"]?.toInt() ?: 12)
            )
        }

        // Save Changes to database
        sentry.span("userRepository", "save") { userRepository.save(userNode) }

        // Create LoginResponse to return
        val tokenData = tokenService.createAccessToken(userNode.refreshToken)
        return LoginResponse(
            userNode.toUser(),
            tokenData.first,
            userNode.refreshToken,
            tokenData.second
        )
//        return LoginResponse(userNode.toUser(), sentry.span("tokenService", "createAccessToken") { tokenService.createAccessToken(userNode.refreshToken) }, userNode.refreshToken)
    }
}