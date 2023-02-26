package app.birdsocial.birdapi.services

import app.birdsocial.birdapi.exceptions.AuthException
import app.birdsocial.birdapi.graphql.types.AuthInput
import app.birdsocial.birdapi.graphql.types.LoginResponse
import app.birdsocial.birdapi.helper.SentryHelper
import app.birdsocial.birdapi.repository.UserRepository
import app.birdsocial.birdapi.repository.UserService
import org.mindrot.jbcrypt.BCrypt
import org.springframework.core.env.Environment
import org.springframework.core.env.get
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class AuthService(
    val sentry: SentryHelper,
    val env: Environment,
    val tokenService: TokenService,

//    val neo4JService: Neo4jService,
    val userService: UserService,
    val userRepository: UserRepository
) {

    fun login(userLogin: AuthInput): LoginResponse { // TODO - Change from LoginResponse to something else as LoginResponse is a GraphQL type
        try {
            // Get user from database with same email
            val userNode = userService.findOneByParam("email", userLogin.email)

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
        // (1.5) Check if user has permission
        // (2) Get Email from database using UserID
        // (3) Check Old Password
        // (4) Update New Password

        // (1) Get UserID from token
        val userId = sentry.span("tkn-srv", "getToken") { tokenService.getToken(access, false).audience[0] }

        // (1.5) Check if user has permission

        // (2) Get Email from database using UserID
        val userNode = userService.findOneById(userId)

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