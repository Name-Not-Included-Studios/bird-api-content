package app.birdsocial.birdapi.graphql.types.user

import app.birdsocial.birdapi.BirdApiApplication
import app.birdsocial.birdapi.graphql.exceptions.BirdException
import app.birdsocial.birdapi.neo4j.schemas.UserNode
import com.opencsv.CSVReader
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.session.SessionFactory
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.io.FileReader
import java.util.*
import kotlin.random.Random.Default.nextInt

@Controller
class UserResolver(val sessionFactory: SessionFactory) {
    // This is the function that gets data from neo4j
    // @SchemaMapping
    @QueryMapping
    fun getUsers(@Argument userSearch: UserSearch): List<User> {
        if (!BirdApiApplication.bucket.tryConsume(1))
            throw BirdException("You are sending too many requests, please wait and try again.")

        val startTime = System.nanoTime()

        // Begin Neo4J Session
        val session = sessionFactory.openSession()

        // Generate Filters
        val usernameEqualsFilter = if (userSearch.usernameEquals != null)
            Filter("username", ComparisonOperator.EQUALS, userSearch.usernameEquals)
        else
            Filter("username", ComparisonOperator.EXISTS)

        val usernameContainsFilter = if (userSearch.usernameContains != null)
            Filter("username", ComparisonOperator.CONTAINING, userSearch.usernameContains)
        else
            Filter("username", ComparisonOperator.EXISTS)

        val displayNameFilter = if (userSearch.displayName != null)
            Filter("displayName", ComparisonOperator.CONTAINING, userSearch.displayName)
        else
            Filter("displayName", ComparisonOperator.EXISTS)

        val bioFilter = if (userSearch.bio != null)
            Filter("bio", ComparisonOperator.CONTAINING, userSearch.bio)
        else
            Filter("bio", ComparisonOperator.EXISTS)

        val finalFilter = usernameEqualsFilter.and(usernameContainsFilter).and(displayNameFilter).and(bioFilter)

        // Query and filter results from the database
        val usersNodes: List<UserNode> = session.loadAll(UserNode::class.java, finalFilter).toList()

        // Convert Neo4j users to GraphQL users
        val users: List<User> = usersNodes.map { user -> user.toUser() }
        val endTime = System.nanoTime()
        println("GetUser T: ${(endTime - startTime) / 1_000_000.0}")

        return users

    }

    @MutationMapping
    fun createUsers(@Argument num: Int): String {
        if (!BirdApiApplication.bucket.tryConsume(1))
            throw BirdException("You are sending too many requests, please wait and try again.")

        val startTime = System.nanoTime()

        val csvReader = CSVReader(FileReader("names.csv"))

        val names = mutableListOf<String>()

        // we are going to read data line by line
        var nextRecord: Array<String>? = csvReader.readNext()
        while (nextRecord != null) {

//            for (cell in nextRecord) {
//                print(cell + "\t")
//            }
//            println()

            names.add(nextRecord[0])

            nextRecord = csvReader.readNext()
        }

        val users = mutableListOf<UserNode>()

        for (i in 0 until num) {
            val index = nextInt(1080)
            val name = names[index]

            val userNode = UserNode(
                UUID.randomUUID().toString(),
                "${name.lowercase()}@example.com",
                name.lowercase(),
                name.uppercase(),
                "",
                "",
                "",
                "",
                false,
                0,
                0,
                0
            )

            users.add(userNode)
        }

        // Begin Neo4J Session
        val session = sessionFactory.openSession()

        session.save(users)

        val endTime = System.nanoTime()
        println("CreateUsers T: ${(endTime - startTime) / 1_000_000.0}")

//        return users.map { user -> user.toUser() }
        return "Success"
    }

    @MutationMapping
    fun createUser(@Argument userCreate: UserCreate): User {
        if (!BirdApiApplication.bucket.tryConsume(5))
            throw BirdException("You are sending too many requests, please wait and try again.")

        val startTime = System.nanoTime()

        // Begin Neo4J Session
        val session = sessionFactory.openSession()

        val userNode = UserNode(
            UUID.randomUUID().toString(),
            userCreate.email,
            userCreate.username,
            userCreate.displayName,
            userCreate.password,
            "",
            "",
            "",
            false,
            0,
            0,
            0
        )

        session.save(userNode)

        val endTime = System.nanoTime()
        println("CreateUser T: ${(endTime - startTime) / 1_000_000.0}")

        return userNode.toUser()


//        val tx = session.beginTransaction()
//
//        try {
//            val userNode = UserNode(
//                UUID.randomUUID().toString(),
//                userCreate.email,
//                userCreate.username,
//                userCreate.displayName,
//                userCreate.password,
//                "",
//                "",
//                "",
//                false,
//                0,
//                0,
//                0
//            )
//
//            tx.commit()
//            session.save(userNode)
//
//            val endTime = System.nanoTime()
//            println("CreateUser T: ${(endTime - startTime) / 1_000_000.0}")
//
//            return userNode.toUser()
//        } catch (e: Exception) {
//            tx.rollback()
//            println("ROLLBACK: $e")
//        } finally {
//            tx.close()
//        }
//
//        throw BirdException("There was an error saving your request.")
    }
}
