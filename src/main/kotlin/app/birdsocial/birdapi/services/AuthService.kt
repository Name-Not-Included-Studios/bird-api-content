package app.birdsocial.birdapi.services

import app.birdsocial.birdapi.config.EnvironmentData
import app.birdsocial.birdapi.exceptions.AuthException
import app.birdsocial.birdapi.exceptions.BirdException
import app.birdsocial.birdapi.graphql.types.AuthInput
import app.birdsocial.birdapi.graphql.types.LoginResponse
import app.birdsocial.birdapi.helper.SentryHelper
import app.birdsocial.birdapi.neo4j.schemas.UserNode
import org.mindrot.jbcrypt.BCrypt
import org.neo4j.ogm.session.SessionFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuthService(
    val sessionFactory: SessionFactory,
    val sentryHelper: SentryHelper,
    val envData: EnvironmentData,
    val tokenService: TokenService,
    val userDataService: UserDataService,
    ) {

    // TODO - Change from LoginResponse to something else as LoginResponse is a GraphQL type
    fun login(userLogin: AuthInput): LoginResponse {
        // Begin Neo4J Session
        val session = sessionFactory.openSession()

        val userNode = sentryHelper.span("user-srv", "loadAll") { userDataService.getNode<UserNode>(userLogin.email, "email", sessionFactory) }
//        val userNodes: List<UserNode> = sentryHelper.span("neo4j", "loadAll") { session.loadAll(UserNode::class.java, filter, Pagination(1, 5)).toList() }

        if (!sentryHelper.span("compute", "checkpw") { BCrypt.checkpw(userLogin.password, userNode.password) })
            throw AuthException()

        // Officially Proven Identity Here
        userNode.lastLogin = LocalDateTime.now()

//        if (userNode.refreshToken == null)
//            userNode.refreshToken = tokenService.createRefreshToken(userNode.userId)

        if (!sentryHelper.span("tkn-srv", "checkToken") { tokenService.checkToken(userNode.refreshToken, true) })
            userNode.refreshToken = sentryHelper.span("tkn-srv", "createRefreshToken") { tokenService.createRefreshToken(userNode.userId) }

        sentryHelper.span("neo4j", "save") { session.save(userNode) }
        return LoginResponse(userNode.toUser(), sentryHelper.span("tkn-srv", "createAccessToken") { tokenService.createAccessToken(userNode.refreshToken) }, userNode.refreshToken)
    }

    fun updatePassword(oldPassword: String, newPassword: String, access: String): LoginResponse {
        val session = sessionFactory.openSession()

        // (1) Get UserID from token
        // (2) Get Email from database using UserID
        // (3) Check Old Password
        // (4) Update New Password

        // Get UserID from token
        val userId = sentryHelper.span("tkn-srv", "getToken") { tokenService.getToken(access, false).audience[0] }

        // Get Email from database using UserID
        val userNode = sentryHelper.span("user-srv", "getNode") { userDataService.getNode<UserNode>(userId, "userId", sessionFactory) }

        if (!sentryHelper.span("compute", "checkpw") { BCrypt.checkpw(oldPassword, userNode.password) })
            throw AuthException()

        // Officially Proven Identity Here
        if (!sentryHelper.span("compute", "checkpw") { BCrypt.checkpw(newPassword, userNode.password) })
            throw BirdException("You Must Change The Password")

        userNode.lastLogin = LocalDateTime.now()
        userNode.password = sentryHelper.span("compute", "hashpw") { BCrypt.hashpw(newPassword, BCrypt.gensalt(envData.getData<Int>("BCRYPT_LOG_ROUNDS"))) }

        if (!sentryHelper.span("tkn-srv", "checkToken") { tokenService.checkToken(userNode.refreshToken, true) })
            userNode.refreshToken = sentryHelper.span("tkn-srv", "createRefreshToken") { tokenService.createRefreshToken(userId) }

        sentryHelper.span("neo4j", "save") { session.save(userNode) }
        return LoginResponse(userNode.toUser(), sentryHelper.span("tkn-srv", "createAccessToken") { tokenService.createAccessToken(userNode.refreshToken) }, userNode.refreshToken)
    }
}