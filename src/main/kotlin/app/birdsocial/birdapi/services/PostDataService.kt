package app.birdsocial.birdapi.services

import app.birdsocial.birdapi.graphql.exceptions.BirdException
import app.birdsocial.birdapi.graphql.types.UserInput
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
class PostDataService(val sessionFactory: SessionFactory) {
    fun getUser(userId: UUID): User {
        return getUserNode(userId).toUser()
    }

    private fun getUserNode(userId: UUID): UserNode {
        // Begin Neo4J Session
        val session = sessionFactory.openSession()

        val filter = Filter("userId", ComparisonOperator.EQUALS, userId.toString())
        val userNodes: List<UserNode> = session.loadAll(UserNode::class.java, filter, Pagination(1, 5)).toList()

        if (userNodes.size > 1)
            throw BirdException("Server Error: Multiple Users Returned")

        val userNode = userNodes[0]
        return userNode
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

    fun updateUser(userId: UUID, userInput: UserInput): User {
        val session = sessionFactory.openSession()

        val userNode = getUserNode(userId)

        if(userInput.username != null)
            userNode.username = userInput.username

        if(userInput.displayName != null)
            userNode.displayName = userInput.displayName

        if(userInput.bio != null)
            userNode.bio = userInput.bio

        if(userInput.websiteUrl != null)
            userNode.websiteUrl = userInput.websiteUrl

        if(userInput.avatarUrl != null)
            userNode.avatarUrl = userInput.avatarUrl

        session.save(userNode)
        return userNode.toUser()
    }

    fun deleteUser(userId: UUID): User {
        val session = sessionFactory.openSession()
        val userNode = getUserNode(userId)
        session.delete(userNode) // TODO - may need to delete relations first
        return userNode.toUser()
    }
}