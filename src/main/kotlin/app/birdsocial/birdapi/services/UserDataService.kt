package app.birdsocial.birdapi.services

import app.birdsocial.birdapi.graphql.exceptions.BirdException
import app.birdsocial.birdapi.graphql.types.ProfileInput
import app.birdsocial.birdapi.graphql.types.User
import app.birdsocial.birdapi.neo4j.schemas.UserNode
import org.mindrot.jbcrypt.BCrypt
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.cypher.query.Pagination
import org.neo4j.ogm.session.SessionFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class DataService(val sessionFactory: SessionFactory) {
    fun getUser(userId: UUID): User {
        // Begin Neo4J Session
        val session = sessionFactory.openSession()

        val filter = Filter("userId", ComparisonOperator.EQUALS, userId.toString())
        val userNodes: List<UserNode> = session.loadAll(UserNode::class.java, filter, Pagination(1, 5)).toList()

        if (userNodes.size > 1)
            throw BirdException("Server Error: Multiple Users Returned")

        val user = userNodes[0].toUser()

        return user
    }

    fun createUser(email: String, password: String): User {
        // Begin Neo4J Session
        val session = sessionFactory.openSession()

        val user: UserNode = UserNode(
            userId= UUID.randomUUID().toString(),
            email= email,
            password= BCrypt.hashpw(password, BCrypt.gensalt(12)),
        )

        session.save(user)

        return user.toUser()
    }

    fun updateUser(userId: UUID, user: ProfileInput): User {
        val session = sessionFactory.openSession()

        val filter = Filter("userId", ComparisonOperator.EQUALS, userId.toString())
        val userNodes: List<UserNode> = session.loadAll(UserNode::class.java, filter, Pagination(1, 5)).toList()

        if (userNodes.size > 1)
            throw BirdException("Server Error: Multiple Users Returned")


    }
}