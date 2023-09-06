package app.birdsocial.birdapi.graphql.resolvers

import app.birdsocial.birdapi.config.ApplicationConfig
import app.birdsocial.birdapi.exceptions.AuthException
import app.birdsocial.birdapi.exceptions.PermissionException
import app.birdsocial.birdapi.graphql.types.Pagination
import app.birdsocial.birdapi.graphql.types.Post
import app.birdsocial.birdapi.graphql.types.PostInput
import app.birdsocial.birdapi.neo4j.schemas.PostNode
import app.birdsocial.birdapi.repository.PostRepository
import app.birdsocial.birdapi.repository.PostService
import app.birdsocial.birdapi.repository.UserRepository
import app.birdsocial.birdapi.repository.UserService
import app.birdsocial.birdapi.services.PermissionService
import app.birdsocial.birdapi.services.SentryHelper
import app.birdsocial.birdapi.services.TokenService
import jakarta.servlet.http.HttpServletRequest
import org.neo4j.cypherdsl.core.Cypher
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.stereotype.Controller
import java.time.Instant
import java.util.*

@Controller
class PostResolver(
    val request: HttpServletRequest,

    val appConfig: ApplicationConfig,

    val permissionService: PermissionService,

    val userRepository: UserRepository,
    val postRepository: PostRepository,
    val userService: UserService,
    val postService: PostService,
    val neo4j: Neo4jTemplate,

    val sentry: SentryHelper,

    val api: ApiHelper,
    val tokenService: TokenService,
) {
    @MutationMapping
    fun createPost(
        @Argument post: PostInput
    ): Post = sentry.captureTransaction {
        // Check if too many requests
        api.throttleRequest(10)
//        if (!envData.bucket.tryConsume(10)) // TODO - Add tokens for image size
//            throw ThrottleRequestException()

        // Get userId from authorization token
        val userId = tokenService.authorize(request)

        // Check if user has permission
        if (permissionService.checkPermission(userId, "bird.post.create"))
            throw PermissionException()

        // Get me from database
        val user = userService.findOneById(userId)

        // Create PostNode
        val postNode = PostNode(
            UUID.randomUUID().toString(),
            post.content,
            post.annotation ?: "",
            Instant.now(),
            Instant.now(),
//            user,
//            null,
//            mutableListOf(),
//            mutableListOf(),
        )

//        user.posts.add(postNode)
        postNode.authors.add(user)

        // Save post to the database
//        sentry.span("userRepository", "save") {
//            userRepository.save(user)
//        }

        return sentry.span("postRepository", "save") {
            postRepository.save(postNode).toPost()
        }
    }

    // TODO - Unprotected
    @MutationMapping
    fun deletePost( // TODO - change to 'visible = false'
        @Argument postId: String
    ): Post = sentry.captureTransaction {
        // Check if too many requests
        api.throttleRequest(5)

        // Get userId from authorization token
        val userId = tokenService.authorize(request)

        // Get post from database
        val post = postService.findOneById(postId)

        // If I didn't author this post (list of authors to search)
        if (post.authors.any { user -> user.id == userId })
            throw AuthException() // Shouldn't happen unless someone tries to delete another users post
//        if (post.authoredBy.id == userId)
//            throw AuthException() // Shouldn't happen unless someone tries to delete another users post

        // Delete it
        sentry.span("postRepository", "delete") { postRepository.delete(post) }

        return post.toPost()
    }

    // TODO - Unprotected
    @MutationMapping
    fun annotatePost(
        @Argument postId: String,
        @Argument annotation: String
    ): Post = sentry.captureTransaction {
        // Check if too many requests
        api.throttleRequest(5)

        // Get userId from authorization token
        val userId = tokenService.authorize(request)

        val post = postService.findOneById(postId)

        // If I didn't author this post (list of authors to search)
        if (post.authors.any { user -> user.id == userId })
            throw AuthException() // Shouldn't happen unless someone tries to delete another users post

        // Set annotation data
        post.annotation = annotation
        post.annotationDate = Instant.now()

        // Save changes to database
        sentry.span("postRepository", "save") { postRepository.save(post) }

        return post.toPost()
    }

    // TODO - Unprotected
    @MutationMapping
    fun likePost(
        @Argument postId: String
    ): Post = sentry.captureTransaction {
        // Check if too many requests
        api.throttleRequest(1)

        // Get userId from authorization token
        val userId = tokenService.authorize(request)

        // Get me from database
        val user = userService.findOneById(userId)

        // Get desired post from database
        val post = postService.findOneById(postId)

        // Set desired like status
        user.liked.add(post)
//        post.likedBy.add(user)

        // Save changes to database
        userRepository.save(user)
//        postRepository.save(post)

        return post.toPost()
    }

    // TODO - Unprotected
    @MutationMapping
    fun unlikePost(
        @Argument postId: String
    ): Post = sentry.captureTransaction {
        // Check if too many requests
        api.throttleRequest(1)

        // Get userId from authorization token
        val userId = tokenService.authorize(request)

        // Get me from database
        val user = userService.findOneById(userId)

        // Get desired post from database
        val post = postService.findOneById(postId)

        // Set desired like status
        user.liked.remove(post)
//        post.likedBy.remove(user)

        // Save changes to database
        userRepository.save(user)
//        postRepository.save(post)

        return post.toPost()
    }

    // TODO - Unprotected
    @QueryMapping
    fun getPost(
        @Argument postId: String
    ): Post = sentry.captureTransaction {
        // Check if too many requests
        api.throttleRequest(1)

        // Get post from database
        return postService.findOneById(postId).toPost()
    }

    // TODO - Unprotected
    @QueryMapping
    fun getRecentPostsFromUser(
        @Argument userId: String,
        @Argument page: Int,
        @Argument pageSize: Int
    ): List<Post> = sentry.captureTransaction {
        // Check if too many requests
        api.throttleRequest(5)

        // Limit page size
        val pageSizeLimited = if (pageSize > 50) 50 else pageSize

        sentry.span<Nothing>("neo4j", "getPost") {
            // MATCH (:User {id:$userId})-[:AUTHORED]->(post:Post) RETURN post SKIP ${page * pageSizeLimited} LIMIT $pageSizeLimited
            val user = Cypher.node("User").withProperties(mapOf(Pair("id", userId)))
            val post = Cypher.node("Post").named("post")
            val request = Cypher.match(user.relationshipTo(post, "AUTHORED"))
                .returning(post)
                .skip(page * pageSizeLimited)
                .limit(pageSizeLimited)
                .build()

            println(request.cypher)
            return neo4j.findAll(request.cypher, PostNode::class.java).map { p -> p.toPost() }
        }
    }

    // TODO - Unprotected
    @QueryMapping
    fun getTimeline(
        @Argument pagination: Pagination
    ): List<Post> = sentry.captureTransaction {
        // Check if too many requests
        api.throttleRequest(20) // was 5

        // Get userId from authorization token
        val userId = tokenService.authorize(request)

        sentry.span<Nothing>("user-srv", "getFollowedPosts") {
            // MATCH (me:User {id:$userId})-[:FOLLOWING]->(:User)->[:AUTHORED]->(posts:Post) RETURN posts SKIP ${page * pageSizeLimited} LIMIT $pageSizeLimited
            val me = Cypher.node("User")
                .named("me")
                .withProperties(mapOf(Pair("id", userId)))
            val users = Cypher.node("User")
            val posts = Cypher.node("Post").named("posts")

            val request = Cypher.match(
                me.relationshipTo(users, "FOLLOWING")
                    .relationshipTo(posts, "AUTHORED")
            ).returning(posts)
                .skip(pagination.page * pagination.pageSize.coerceIn(1, appConfig.maxPageSize))
                .limit(pagination.pageSize.coerceIn(1, appConfig.maxPageSize))
                .build()

            return neo4j.findAll(request, PostNode::class.java).map { p -> p.toPost() }
        }
    }
}