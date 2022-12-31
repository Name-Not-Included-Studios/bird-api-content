package app.birdsocial.birdapi.services

import app.birdsocial.birdapi.graphql.exceptions.AuthException
import app.birdsocial.birdapi.graphql.exceptions.BirdException
import app.birdsocial.birdapi.graphql.types.AuthInput
import app.birdsocial.birdapi.neo4j.schemas.UserNode
import org.mindrot.jbcrypt.BCrypt
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.cypher.query.Pagination
import org.neo4j.ogm.session.SessionFactory
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class AuthService(val sessionFactory: SessionFactory, val tokenService: TokenService) {
    fun login(userLogin: AuthInput): Pair<String, String> {

        // Begin Neo4J Session
        val session = sessionFactory.openSession()

        val filter = Filter("email", ComparisonOperator.EQUALS, userLogin.email)
        val userNodes: List<UserNode> = session.loadAll(UserNode::class.java, filter, Pagination(1, 5)).toList()

        if (userNodes.size > 1)
            throw BirdException("Server Error: Multiple Users Returned")

        val userNode = userNodes[0]

        if (!BCrypt.checkpw(userLogin.password, userNode.password))
            throw AuthException()

        // Officially Proven Identity Here
        userNode.lastLogin = LocalDateTime.now()

        if (!tokenService.checkToken(userNode.refreshToken, true))
            userNode.refreshToken = tokenService.createRefreshToken()

        session.save(userNode)
        return Pair(userNode.refreshToken, tokenService.createAccessToken(userNode.refreshToken))
    }
}