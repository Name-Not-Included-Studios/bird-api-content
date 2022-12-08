package app.birdsocial.birdapi.graphql.user

import app.birdsocial.birdapi.graphql.exceptions.BirdException
import app.birdsocial.birdapi.neo4j.schemas.UserGQL
import app.birdsocial.birdapi.neo4j.schemas.UserN4J
import org.neo4j.ogm.session.SessionFactory
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller

@Controller
class UserResolver(val sessionFactory: SessionFactory) {
    // This is the function that gets data from neo4j
    // @SchemaMapping
    @QueryMapping
    fun users(@Argument userArgs: UserArgs): List<UserGQL> {
        if (true) { // BirdApiApplication.bucket.tryConsume(1)
            val startTime = System.nanoTime()

            // Begin Neo4J Session
            val session = sessionFactory.openSession()

            // Query and filter results from the database
            val usersN4JUnfiltered = session.loadAll(UserN4J::class.java).toList()
            val filterTime = System.nanoTime()
            val usersN4J =
                    usersN4JUnfiltered.filter { user ->
                        !((userArgs.username != null && userArgs.username != user.username) ||
                                (userArgs.displayName != null &&
                                        userArgs.displayName != user.displayName) ||
                                (userArgs.bio != null && userArgs.bio != user.bio) ||
                                (userArgs.isVerified != null &&
                                        userArgs.isVerified != user.isVerified) ||
                                (userArgs.chirpCount != null &&
                                        userArgs.chirpCount != user.chirpCount) ||
                                (userArgs.followersCount != null &&
                                        userArgs.followersCount != user.followersCount) ||
                                (userArgs.followingCount != null &&
                                        userArgs.followingCount != user.followingCount))
                    }

            // Convert Neo4j users to GraphQL users
            val users: List<UserGQL> = usersN4J.map { user -> user.toUserGQL() }
            //			println("USER")
            val endTime = System.nanoTime()
            println(
                    "T: ${(endTime-startTime)/1_000_000.0} D: ${(filterTime-startTime)/1_000_000.0} F: ${(endTime-filterTime)/1_000_000.0}"
            )

            return users
        }

        throw BirdException("You are sending too many requests, please wait and try again.")
    }
}
