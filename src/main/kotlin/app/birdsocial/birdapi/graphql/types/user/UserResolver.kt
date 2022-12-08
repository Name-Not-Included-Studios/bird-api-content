package app.birdsocial.birdapi.graphql.schemas.user

import app.birdsocial.birdapi.graphql.exceptions.BirdException
import app.birdsocial.birdapi.neo4j.schemas.UserNode
import org.neo4j.ogm.session.SessionFactory
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.util.*

@Controller
class UserResolver(val sessionFactory: SessionFactory) {
    // This is the function that gets data from neo4j
    // @SchemaMapping
    @QueryMapping
    fun getUsers(@Argument userSearch: UserSearch): List<User> {
        if (true) { // BirdApiApplication.bucket.tryConsume(1)
            val startTime = System.nanoTime()

            // Begin Neo4J Session
            val session = sessionFactory.openSession()

            // Query and filter results from the database
            val usersN4JUnfiltered = session.loadAll(UserNode::class.java).toList()
            val filterTime = System.nanoTime()
            val usersN4J =
                    usersN4JUnfiltered.filter { user ->
                        !((userSearch.username != null && userSearch.username != user.username) ||
                                (userSearch.displayName != null &&
                                        userSearch.displayName != user.displayName) ||
                                (userSearch.bio != null && userSearch.bio != user.bio) ||
                                (userSearch.isVerified != null &&
                                        userSearch.isVerified != user.isVerified) ||
                                (userSearch.chirpCount != null &&
                                        userSearch.chirpCount != user.chirpCount) ||
                                (userSearch.followersCount != null &&
                                        userSearch.followersCount != user.followersCount) ||
                                (userSearch.followingCount != null &&
                                        userSearch.followingCount != user.followingCount))
                    }

            // Convert Neo4j users to GraphQL users
            val users: List<User> = usersN4J.map { user -> user.toUser() }
            //			println("USER")
            val endTime = System.nanoTime()
            println(
                    "T: ${(endTime-startTime)/1_000_000.0} D: ${(filterTime-startTime)/1_000_000.0} F: ${(endTime-filterTime)/1_000_000.0}"
            )

            return users
        }

        throw BirdException("You are sending too many requests, please wait and try again.")
    }

    @MutationMapping
    fun createUser(@Argument userCreate: UserCreate): User {
        return User(
            UUID.randomUUID(),
            userCreate.email,
            userCreate.username,
            userCreate.displayName,
            userCreate.password
        )
    }
}
