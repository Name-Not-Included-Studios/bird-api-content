package app.birdsocial.birdapi.graphql.types.user

import app.birdsocial.birdapi.BirdApiApplication
import app.birdsocial.birdapi.graphql.exceptions.BirdException
import app.birdsocial.birdapi.neo4j.schemas.PostNode
import app.birdsocial.birdapi.neo4j.schemas.UserNode
import com.opencsv.CSVReader
import org.mindrot.jbcrypt.BCrypt
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.cypher.query.Pagination
import org.neo4j.ogm.session.SessionFactory
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.io.FileReader
import java.util.*
import kotlin.random.Random.Default.nextDouble
import kotlin.random.Random.Default.nextInt
import kotlin.system.measureTimeMillis

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
        val usernameEqualsFilter = if (userSearch.usernameEquals != null && userSearch.usernameEquals != "")
            Filter("username", ComparisonOperator.EQUALS, userSearch.usernameEquals)
        else
            Filter("username", ComparisonOperator.EXISTS)

        val usernameContainsFilter = if (userSearch.usernameContains != null && userSearch.usernameContains != "")
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

        val finalFilter = usernameEqualsFilter
            .and(usernameContainsFilter)
            .and(displayNameFilter)
            .and(bioFilter)

        // Query and filter results from the database
        val usersNodes: List<UserNode> = session.loadAll(UserNode::class.java, finalFilter, Pagination(0, 25)).toList()
        if (usersNodes.isEmpty())
            throw BirdException("No Users Found")

        // Convert Neo4j users to GraphQL users
        val users: List<User> = usersNodes.map { user -> user.toUser() }
        val endTime = System.nanoTime()
        println("GetUser T: ${(endTime - startTime) / 1_000_000.0}")
        println("FollowedBy: ${usersNodes[0].followedBy.size}")

        return users
    }

    fun login(userLogin: UserCreate): String {
        if (!BirdApiApplication.bucket.tryConsume(50))
            throw BirdException("You are sending too many requests, please wait and try again.")

        val time = measureTimeMillis {
            // Begin Neo4J Session
            val session = sessionFactory.openSession()

            val filter = Filter("email", ComparisonOperator.EQUALS, userLogin.email)
            val userNodes: List<UserNode> = session.loadAll(UserNode::class.java, filter, Pagination(1, 10)).toList()

            if (userNodes.size > 1)
                throw BirdException("Server Error: Multiple Users")

            val userNode = userNodes[0]

            if (!BCrypt.checkpw(userLogin.password, userNode.password))
                throw BirdException("Bad Password")



            session.save(userNode)
        }
        println("Login User T: $time")

        return "Login Successful"
    }

    ///*
    @MutationMapping
    fun createUsers(@Argument num: Int): String {
        if (!BirdApiApplication.bucket.tryConsume(2)) //200
            throw BirdException("You are sending too many requests, please wait and try again.")

        val startTime = System.nanoTime()

        val csvReader = CSVReader(FileReader("names.csv"))

        val names = mutableListOf<String>()

        // we are going to read data line by line
        var nextRecord: Array<String>? = csvReader.readNext()
        while (nextRecord != null) {
            names.add(nextRecord[0])
            nextRecord = csvReader.readNext()
        }

        val users = mutableListOf<UserNode>()

        var totalDegree: Int = 0

        for (i in 0 until num) {
            val name1 = names[nextInt(1080)]
            val name2 = names[nextInt(1080)]

            val userNode = UserNode(
                UUID.randomUUID().toString(),
                "${name1.lowercase()}-${name2.lowercase()}@example.com",
                "${name1.lowercase()}-${name2.lowercase()}",
                "${name1.uppercase()} ${name2.uppercase()}",
                BCrypt.hashpw("12345678", BCrypt.gensalt(12)),
            )

            println("CreateUser: $i")

            if (i > 1) {
//                println("Add Other $totalDegree")
                var idx = 0
                var r: Double = nextDouble(totalDegree.toDouble())
                while (idx < users.size - 1) {
                    r -= users.get(idx).followedBy.size
                    if (r <= 0.0) break
                    ++idx
                }
                val otherNode: UserNode = users[idx]
                userNode.following.add(otherNode)
                otherNode.followedBy.add(userNode)
                totalDegree += 2
            } else if (i > 0) {
//                println("Add Second $totalDegree")
                val otherNode: UserNode = users[0]
                userNode.following.add(otherNode)
                otherNode.followedBy.add(userNode)
                totalDegree += 2
            } else {
//                println("Add First $totalDegree")
            }

            for (j in 1 until nextInt(10)) {
                val post = PostNode(
                    UUID.randomUUID().toString(),
                    "${userNode.displayName}'s Post $j",
                    "The post body can be much longer, I love cheese"
                )

                userNode.authored.add(post)
                post.authoredBy.add(userNode)
            }

            users.add(userNode)
        }

        // Begin Neo4J Session
        val session = sessionFactory.openSession()

        session.save(users)

        val endTime = System.nanoTime()
        println("CreateUsers T: ${(endTime - startTime) / 1_000_000.0}")

        return "Success"
    }
    //*/

    @MutationMapping
    fun createUser(@Argument userCreate: UserCreate): User {
        if (!BirdApiApplication.bucket.tryConsume(200))
            throw BirdException("You are sending too many requests, please wait and try again.")

        val userNode: UserNode

        val time = measureTimeMillis {
            // Begin Neo4J Session
            val session = sessionFactory.openSession()

            val emailFilter = Filter("email", ComparisonOperator.EQUALS, userCreate.username)
            val emailTaken = session.loadAll(UserNode::class.java, emailFilter, Pagination(0, 10)).isNotEmpty()
            val usernameFilter = Filter("username", ComparisonOperator.EQUALS, userCreate.username)
            val usernameTaken = session.loadAll(UserNode::class.java, usernameFilter, Pagination(0, 10)).isNotEmpty()

            if (emailTaken)
                throw BirdException("Email Already Used")
            if (usernameTaken)
                throw BirdException("Username Taken")

            userNode = UserNode(
                UUID.randomUUID().toString(),
                userCreate.email,
                userCreate.username,
                userCreate.displayName,
                BCrypt.hashpw(userCreate.password, BCrypt.gensalt(16)),
            )

            session.save(userNode)
        }
        println("CreateUser T: $time")

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
