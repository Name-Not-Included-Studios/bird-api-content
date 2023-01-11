package app.birdsocial.birdapi.services

import app.birdsocial.birdapi.config.EnvironmentData
import app.birdsocial.birdapi.exceptions.BirdException
import app.birdsocial.birdapi.graphql.types.Post
import app.birdsocial.birdapi.graphql.types.User
import app.birdsocial.birdapi.graphql.types.UserInput
import app.birdsocial.birdapi.helper.SentryHelper
import app.birdsocial.birdapi.neo4j.schemas.PostNode
import app.birdsocial.birdapi.neo4j.schemas.UserNode
import org.mindrot.jbcrypt.BCrypt
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.cypher.Filters
import org.neo4j.ogm.cypher.query.Pagination
import org.neo4j.ogm.session.SessionFactory
import org.springframework.stereotype.Service
import java.util.*
//sentryHelper.captureSpan ("db", "userDataService") {
@Service
class UserDataService(
    val sessionFactory: SessionFactory,
    val tokenService: TokenService,
    val envData: EnvironmentData,
    val sentryHelper: SentryHelper,
    ) : DataService() {
    fun getUser(userId: String): User {
        return sentryHelper.span("neo4j", "getNode") { getNode<UserNode>(userId, "userId", sessionFactory).toUser() }
    }

    fun getUserPosts(userId: String, pageNumber: Int, pageSize: Int): List<Post> {
        val session = sessionFactory.openSession()

        val pagination: Pagination = Pagination(pageNumber, pageSize)

        // Create/load a map to hold the parameter
        // Caches execution path instead of reparsing cypher
        val params: MutableMap<String, Any> = HashMap(1)
        params["userId"] = userId

        var postNodes = sentryHelper.span("neo4j", "query") { session.query(PostNode::class.java,
            "MATCH (u:User {userId: \$userId})-[r:AUTHORED]->(p:Post)" +
                    "RETURN p ORDER BY p.creationDate DESC $pagination",
            params
        )}

        // Load Relations
        postNodes = sentryHelper.span("neo4j", "load") { postNodes.map { session.load(PostNode::class.java, it.id) } }

        return postNodes.map { pn -> pn.toPost() }
    }

    fun getUserPosts(userId: String, filter: Filter, pagination: Pagination = Pagination(0, 25)): List<Post> {
        return sentryHelper.span("neo4j", "getNode") { getNode<UserNode>(userId, "userId", sessionFactory).posts.map { postNode -> postNode.toPost() } }
    }

    fun getUserPosts(userId: String, filters: Filters, pagination: Pagination = Pagination(0, 25)): List<Post> {
        return sentryHelper.span("neo4j", "getNode") { getNode<UserNode>(userId, "userId", sessionFactory).posts.map { postNode -> postNode.toPost() } }
    }

    fun getUser(filter: Filter): User {
        return getUsers(filter, Pagination(0, 2))[0]
    }

    fun getUser(filters: Filters): User {
        return getUsers(filters, Pagination(0, 2))[0]
    }

    fun getUsers(filter: Filter, pagination: Pagination = Pagination(0, 25)): List<User> {
        val session = sessionFactory.openSession()
        return sentryHelper.span("neo4j", "loadAll") { session.loadAll(UserNode::class.java, filter, pagination).map { userNode -> userNode.toUser() } }
    }

    fun getUsers(filters: Filters, pagination: Pagination = Pagination(0, 25)): List<User> {
        val session = sessionFactory.openSession()
        return sentryHelper.span("neo4j", "loadAll") { session.loadAll(UserNode::class.java, filters, pagination).map { userNode -> userNode.toUser() } }
    }

    fun getFollowedPosts(userId: String, page: Int, pageSize: Int): List<Post> {
        val session = sessionFactory.openSession()
        return session.query(PostNode::class.java,
            "MATCH (:User {userId:\"$userId\"})-[:FOLLOWING]->(:User)-[:AUTHORED]->(p:Post) " +
                    "RETURN p ORDER BY p.creationDate DESC " +
                    "SKIP ${page * pageSize} LIMIT $pageSize",
            HashMap<String, Any>(0)
        ).map { p -> p.toPost() }
    }

    fun getRefreshToken(userId: String): String {
        return sentryHelper.span("neo4j", "getNode") { getNode<UserNode>(userId, "userId", sessionFactory).refreshToken }
    }

    fun createUser(userId: String, email: String, password: String): User {
        // Begin Neo4J Session
        val session = sessionFactory.openSession()

        val user = UserNode(
            userId = userId,
            email = email,
            password = sentryHelper.span("compute", "hashpw") { BCrypt.hashpw(password, BCrypt.gensalt(envData.getData<Int>("BCRYPT_LOG_ROUNDS"))) },
            refreshToken = sentryHelper.span("tkn-srv", "createRefreshToken") { tokenService.createRefreshToken(userId) }
        )

        sentryHelper.span("neo4j", "save") { session.save(user) }
        return user.toUser()
    }

    fun updateUser(userId: String, userInput: UserInput): User {
        val session = sessionFactory.openSession()

        val userNode = sentryHelper.span("neo4j", "getNode") { getNode<UserNode>(userId, "userId", sessionFactory) }

        if(userInput.username != null) {
//            if(sentryHelper.span("usr-srv", "getUsers") { getUsers(Filter("username", ComparisonOperator.EQUALS, userInput.username)).isNotEmpty() })
            if(sentryHelper.span("neo4j", "getNodes") { getNodes<UserNode>(userInput.username, "username", sessionFactory, pageSize = 2).isNotEmpty() })
                throw BirdException("Username Already in Use")

            userNode.username = userInput.username
        }

        if(userInput.displayName != null)
            userNode.displayName = userInput.displayName

        if(userInput.bio != null)
            userNode.bio = userInput.bio

        if(userInput.websiteUrl != null)
            userNode.websiteUrl = userInput.websiteUrl

        if(userInput.avatarUrl != null)
            userNode.avatarUrl = userInput.avatarUrl

        sentryHelper.span("neo4j", "save") { session.save(userNode) }
        return userNode.toUser()
    }

    fun setFollowUser(userIdFollower: String, userIdFollowee: String, value: Boolean): User {
        val session = sessionFactory.openSession()

        val follower = sentryHelper.span("neo4j", "getNode") { getNode<UserNode>(userIdFollower, "userId", sessionFactory) }
        val followee = sentryHelper.span("neo4j", "getNode") { getNode<UserNode>(userIdFollowee, "userId", sessionFactory) }
        follower.following.add(followee)
        followee.followedBy.add(follower)

        sentryHelper.span("neo4j", "save") { session.save(follower) }
        sentryHelper.span("neo4j", "save") { session.save(followee) }

        return followee.toUser()
    }

    fun setLikedPost(userId: String, postId: String, value: Boolean): Post {
        val session = sessionFactory.openSession()

        val user = sentryHelper.span("neo4j", "getNode") { getNode<UserNode>(userId, "userId", sessionFactory) }
        val post = sentryHelper.span("neo4j", "getNode") { getNode<PostNode>(postId, "postId", sessionFactory) }
        user.liked.add(post)
        post.likedBy.add(user)

        sentryHelper.span("neo4j", "save") { session.save(user) }
        sentryHelper.span("neo4j", "save") { session.save(post) }

        return post.toPost()
    }

    fun deleteUser(userId: String): User {
        val session = sessionFactory.openSession()
        val userNode = sentryHelper.span("neo4j", "getNode") { getNode<UserNode>(userId, "userId", sessionFactory) }
        sentryHelper.span("neo4j", "delete") { session.delete(userNode) } // TODO - may need to delete relations first
        return userNode.toUser()
    }
}