package app.birdsocial.birdapi.graphql

import app.birdsocial.birdapi.exceptions.*
import app.birdsocial.birdapi.graphql.types.*
import app.birdsocial.birdapi.helper.SentryHelper
import app.birdsocial.birdapi.neo4j.repo.Neo4jRepository
import app.birdsocial.birdapi.neo4j.repo.PostRepository
import app.birdsocial.birdapi.neo4j.repo.UserRepository
import app.birdsocial.birdapi.neo4j.schemas.PostNode
import app.birdsocial.birdapi.neo4j.schemas.UserNode
import app.birdsocial.birdapi.services.*
import io.github.bucket4j.local.LocalBucket
//import io.sentry.spring.tracing.SentrySpan
//import io.sentry.spring.tracing.SentryTransaction
import jakarta.servlet.http.HttpServletRequest
import org.mindrot.jbcrypt.BCrypt
import org.neo4j.cypherdsl.core.Cypher
import org.passay.*
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Controller
import java.time.LocalDateTime
import java.util.*


@Controller
class ApiGateway(
    val request: HttpServletRequest,

    val userRepository: UserRepository,
    val postRepository: PostRepository,
    val neo4jRepository: Neo4jRepository,
    val neo4j: Neo4jTemplate,

    val bucket: LocalBucket,
    val sentry: SentryHelper,
    val authService: AuthService,
    val tokenService: TokenService,
) {
    @QueryMapping
    fun apiVersion(): String = captureQueryTransaction {
        return "0.0.2"
    }

//    @QueryMapping
//    fun searchUsers(@Argument query: UserSearchCriteria): List<User> = captureQueryTransaction {
//        if (!envData.bucket.tryConsume(1))
//            throw ThrottleRequestException()
//
//        val usernameContainsFilter = Filter("username", ComparisonOperator.CONTAINING, query.usernameContains)
//        val usernameStartsWithFilter =
//            Filter("username", ComparisonOperator.STARTING_WITH, query.usernameStartsWith)
//        val usernameEndsWithFilter = Filter("username", ComparisonOperator.ENDING_WITH, query.usernameEndsWith)
//        val usernameFollowerCountGreaterThanFilter =
//            Filter("followersCount", ComparisonOperator.GREATER_THAN_EQUAL, query.followerCountGreaterThan)
//        val usernameFollowerCountLessThanFilter =
//            Filter("followersCount", ComparisonOperator.LESS_THAN_EQUAL, query.followerCountLessThan)
//        val bioContainsFilter = Filter("bio", ComparisonOperator.CONTAINING, query.bioContains)
//        val usernameFuzzyFilter =
//            Filter("username", ComparisonOperator.LIKE, query.usernameFuzzySearch) // TODO - Change to Fuzzy Search
//
//        val filter = usernameContainsFilter.and(usernameStartsWithFilter)
//            .and(usernameEndsWithFilter).and(usernameFollowerCountGreaterThanFilter)
//            .and(usernameFollowerCountLessThanFilter).and(bioContainsFilter).and(usernameFuzzyFilter)
//
//        return sentryHelper.span("user-srv", "getUsers") { userDataService.getUsers(filter) }
//    }

    private fun throttleRequest(numTokens: Long) {
        println("Tokens: ${bucket.availableTokens}")

        if (!bucket.tryConsume(numTokens))
            throw ThrottleRequestException()
    }

    private fun authorize(): String {
        // Get Authorization Header
        val access = request.getHeader(HttpHeaders.AUTHORIZATION) ?: throw AuthException()
        // Get User ID from refresh token (also checks if signature is valid)
        return sentry.span("tkn-srv", "getToken") { tokenService.getToken(access, false).audience[0] }
    }

    @QueryMapping
    fun getMe(): User = captureQueryTransaction {
        // Check if too many requests
        throttleRequest(1)

//        // Get Authorization Header
//        val access = request.getHeader(HttpHeaders.AUTHORIZATION) ?: throw AuthException()
//        // Get User ID from refresh token (also checks if signature is valid)
//        val userId = sentry.span("tkn-srv", "getToken") { tokenService.getToken(access, false).audience[0] }

        // Get userId from authorization token
        val userId = authorize()

        // Get User from Database
        return neo4jRepository.findOneById<UserNode>("User", userId).toUser()
    }

    // TODO - Fix ck
    @QueryMapping
    fun refresh(): String = captureQueryTransaction {
        // Check if too many requests
        throttleRequest(50)

        // Get Authorization Header
        val refresh = request.getHeader(HttpHeaders.AUTHORIZATION) ?: throw AuthException()
        // Get User ID from refresh token (also checks if signature is valid)
        val userId = sentry.span("tkn-srv", "getToken") { tokenService.getToken(refresh, true).audience[0] }

        // Get DB Refresh Token
        val storedRefresh = neo4jRepository.findOneById<UserNode>("User", userId).refreshToken

        // Check Token against database
        if (refresh != storedRefresh)
            throw AuthException()

        // Create Access Token Based on the Refresh Token
        return sentry.span(
            "tkn-srv",
            "createAccessToken"
        ) { tokenService.createAccessToken(refresh) }
    }

    @MutationMapping
    fun login(@Argument auth: AuthInput): LoginResponse = captureQueryTransaction {
        // Check if too many requests
        throttleRequest(50)

        // Check password for repetitions, sequences, length, etc
        sentry.span("compute", "checkPasswordValidity") { checkPasswordStupidity(auth.password) }

        return sentry.span("auth-srv", "login") { authService.login(auth) }
    }

    @MutationMapping
    fun createAccount(@Argument auth: AuthInput): LoginResponse = captureQueryTransaction {
        // Check if too many requests
        throttleRequest(200)

        // Check password for repetitions, sequences, length, etc
        sentry.span("compute", "checkPasswordValidity") { checkPasswordStupidity(auth.password) }

        // Check that no other user has this email
        sentry.span("neo4j", "getUser") {
            if (neo4jRepository.findAllByParam<UserNode>("User", "email", auth.email, 0, 2).isNotEmpty())
                throw BirdException("Email Already in Use")
        }

        // Create userId and refresh token
        val userId = UUID.randomUUID().toString()
        val refresh = tokenService.createRefreshToken(userId)

        // Generate node based on user information
        val userNode = UserNode(
            userId,
            auth.email,
            "",
            "",
            auth.password,
            refresh,
            LocalDateTime.now(),
            LocalDateTime.now(),
            "",
            "",
            "",
//            mutableListOf(),
//            mutableListOf(),
//            mutableListOf(),
//            mutableListOf(),
        )

        return LoginResponse(
            sentry.span("user-srv", "createUser") {
                userRepository.save<UserNode>(userNode).toUser()
            },
            sentry.span("tkn-srv", "createAccessToken") { tokenService.createAccessToken(refresh) },
            refresh
        )
    }

    @MutationMapping
    fun updatePassword(
        @Argument oldPassword: String,
        @Argument newPassword: String
    ): LoginResponse = captureQueryTransaction {
        // Check if too many requests
        throttleRequest(200)

        // Check password for repetitions, sequences, length, etc
        sentry.span("compute", "checkPasswordValidity") { checkPasswordStupidity(newPassword) }

        // Must actually change password
        if (sentry.span("compute", "checkpw") { oldPassword == newPassword })
            throw BirdException("You Must Change The Password")

        // Get access token
        val access = request.getHeader(HttpHeaders.AUTHORIZATION) ?: throw AuthException()

        // Update Password
        return sentry.span("auth-srv", "updatePassword") {
            authService.updatePassword(
                oldPassword,
                newPassword,
                access
            )
        }
    }

    fun checkPasswordStupidity(password: String): Boolean {
        // Create Validator
        val validator = sentry.span("compute", "createPasswordValidator") {
            PasswordValidator(
                // TODO - DictionaryRule
                LengthRule(8, 32),
                CharacterRule(EnglishCharacterData.UpperCase, 1),
                CharacterRule(EnglishCharacterData.LowerCase, 1),
                CharacterRule(EnglishCharacterData.Digit, 1),
                CharacterRule(EnglishCharacterData.Special, 1),
//            DictionaryRule(),
//            UsernameRule(true, true),
                RepeatCharactersRule(),
                IllegalSequenceRule(EnglishSequenceData.Alphabetical, 4, false),
                IllegalSequenceRule(EnglishSequenceData.Numerical, 4, false),
                IllegalSequenceRule(EnglishSequenceData.USQwerty, 4, false),
                WhitespaceRule(),
            )
        }

        // Validate
        val passwordResult = sentry.span("compute", "validate") { validator.validate(PasswordData(password)) }

        // If not valid, tell user why
        if (!passwordResult.isValid)
            throw BirdException("Password Not Valid: ${validator.getMessages(passwordResult)}")

        return true
    }

    // deleteUser() requires access token as well as email and password
    @MutationMapping
    fun deleteUser(@Argument auth: AuthInput): Boolean =
        // Check if too many requests
        captureQueryTransaction { // TODO - Leaves us with nothing to track down bad actors
            throttleRequest(350)

            // Get userId from authorization token
            val userId = authorize()

            // Using login functionality to ensure that the user has correct authentication
            val userToDelete = sentry.span(
                "user-srv",
                "login"
            ) {
                authService.login(auth).user.userId
            }

            // Check given id matches the database
            if (userId != userToDelete)
                throw AuthException()

            // Delete User by this id
            sentry.span<Boolean>(
                "user-srv",
                "deleteUser"
            ) {
                userRepository.deleteById(userToDelete)
                return true
            }
        }

    @MutationMapping
    fun createPost(
        @Argument post: PostInput
    ): Post = captureQueryTransaction {
        // Check if too many requests
        throttleRequest(10)
//        if (!envData.bucket.tryConsume(10)) // TODO - Add tokens for image size
//            throw ThrottleRequestException()

        // Get userId from authorization token
        val userId = authorize()

        // Get me from database
        val user = neo4jRepository.findOneById<UserNode>("User", userId)

        // Create PostNode
        val postNode = PostNode(
            UUID.randomUUID().toString(),
            post.content,
            post.annotation ?: "",
            LocalDateTime.now(),
            LocalDateTime.now(),
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

    @MutationMapping
    fun deletePost( // TODO - change to 'visible = false'
        @Argument postId: String
    ): Post = captureQueryTransaction {
        // Check if too many requests
        throttleRequest(5)

        // Get userId from authorization token
        val userId = authorize()

        // Get post from database
        val post = neo4jRepository.findOneById<PostNode>("Post", postId)

        // If I didn't author this post (list of authors to search)
        if (post.authors.any { user -> user.id == userId })
            throw AuthException() // Shouldn't happen unless someone tries to delete another users post
//        if (post.authoredBy.id == userId)
//            throw AuthException() // Shouldn't happen unless someone tries to delete another users post

        // Delete it
        sentry.span("postRepository", "delete") { postRepository.delete(post) }

        return post.toPost()
    }

    @MutationMapping
    fun annotatePost(
        @Argument postId: String,
        @Argument annotation: String
    ): Post = captureQueryTransaction {
        // Check if too many requests
        throttleRequest(5)

        // Get userId from authorization token
        val userId = authorize()

        val post = neo4jRepository.findOneById<PostNode>("Post", postId)

        // If I didn't author this post (list of authors to search)
        if (post.authors.any { user -> user.id == userId })
            throw AuthException() // Shouldn't happen unless someone tries to delete another users post

        // Set annotation data
        post.annotation = annotation
        post.annotationDate = LocalDateTime.now()

        // Save changes to database
        sentry.span("postRepository", "save") { postRepository.save(post) }

        return post.toPost()
    }

    @MutationMapping
    fun followUser(
        @Argument followerId: String
    ): User {//= captureQueryTransaction {
        // Check if too many requests
        throttleRequest(1)

        // Get userId from authorization token
        val userId = authorize()

        // Get me from database
        val user = neo4jRepository.findOneById<UserNode>("User", userId)

        // Get followee from database
        val followee = neo4jRepository.findOneById<UserNode>("User", followerId)

        // Set my following status
        user.following.add(followee)
//        followee.followedBy.add(user)

        // Save changes to database
        userRepository.save(user)
//        userRepository.save(followee)

        return followee.toUser()
    }

    @MutationMapping
    fun unfollowUser(
        @Argument followerId: String
    ): User {// = captureQueryTransaction {
        // Check if too many requests
        throttleRequest(1)

        // Get userId from authorization token
        val userId = authorize()

        // Get me from database
        val user = neo4jRepository.findOneById<UserNode>("User", userId)

        // Get followee from database
        val followee = neo4jRepository.findOneById<UserNode>("User", followerId)

        // Remove followee from my following list
        user.following.remove(followee)
//        followee.followedBy.remove(user)

        // Save changes to database
        userRepository.save(user)
//        userRepository.save(followee)

        return followee.toUser()
    }

    @MutationMapping
    fun likePost(
        @Argument postId: String
    ): Post = captureQueryTransaction {
        // Check if too many requests
        throttleRequest(1)

        // Get userId from authorization token
        val userId = authorize()

        // Get me from database
        val user = neo4jRepository.findOneById<UserNode>("User", userId)

        // Get desired post from database
        val post = neo4jRepository.findOneById<PostNode>("Post", postId)

        // Set desired like status
        user.liked.add(post)
//        post.likedBy.add(user)

        // Save changes to database
        userRepository.save(user)
//        postRepository.save(post)

        return post.toPost()
    }

    @MutationMapping
    fun unlikePost(
        @Argument postId: String
    ): Post = captureQueryTransaction {
        // Check if too many requests
        throttleRequest(1)

        // Get userId from authorization token
        val userId = authorize()

        // Get me from database
        val user = neo4jRepository.findOneById<UserNode>("User", userId)

        // Get desired post from database
        val post = neo4jRepository.findOneById<PostNode>("Post", postId)

        // Set desired like status
        user.liked.remove(post)
//        post.likedBy.remove(user)

        // Save changes to database
        userRepository.save(user)
//        postRepository.save(post)

        return post.toPost()
    }

    @QueryMapping
    fun getPost(
        @Argument postId: String
    ): Post = captureQueryTransaction {
        // Check if too many requests
        throttleRequest(1)

        // Get post from database
        return neo4jRepository.findOneById<PostNode>("Post", postId).toPost()
    }

    @MutationMapping
    fun updateUser(
        @Argument user: UserInput
    ): User = captureQueryTransaction {
        // Check if too many requests
        throttleRequest(5)

        // Get userId from authorization token
        val userId = authorize()

        // Get me from database
        val userNode = neo4jRepository.findOneById<UserNode>("User", userId)

        // If user supplied a new username
        if (user.username != null) {
            // Find any user in database with the username supplied

            if (neo4jRepository.findAllByParam<UserNode>("User", "username", user.username, 0, 1).isNotEmpty())
                throw BirdException("Username Already in Use") // TODO - custom exception

            // Set node username
            userNode.username = user.username
        }

        // If user supplied a new displayName, set it
        if (user.displayName != null)
            userNode.displayName = user.displayName

        // If user supplied a new bio, set it
        if (user.bio != null)
            userNode.bio = user.bio

        // If user supplied a new websiteUrl, set it
        if (user.websiteUrl != null)
            userNode.websiteUrl = user.websiteUrl

        // If user supplied a new avatarUrl, set it
        if (user.avatarUrl != null)
            userNode.avatarUrl = user.avatarUrl

        // Save changes to database
        userRepository.save(userNode)

        return userNode.toUser()
    }

    //    @SentryTransaction(operation = "operation-name")
    //    @SentrySpan(operation = "task-name")
    @QueryMapping
    fun getRecentPostsFromUser(
        @Argument userId: String,
        @Argument page: Int,
        @Argument pageSize: Int
    ): List<Post> = captureQueryTransaction {
        // Check if too many requests
        throttleRequest(5)

        // Limit page size
        val pageSizeLimited = if (pageSize > 25) 25 else pageSize

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

    @QueryMapping
    fun getTimeline(
        @Argument page: Int,
        @Argument pageSize: Int,
    ): List<Post> = captureQueryTransaction {
        // Check if too many requests
        throttleRequest(20) // was 5

        // Limit page size
        val pageSizeLimited = if (pageSize > 25) 25 else pageSize

        // Get userId from authorization token
        val userId = authorize()

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
                .skip(page * pageSizeLimited)
                .limit(pageSizeLimited)
                .build()

            return neo4j.findAll(request, PostNode::class.java).map { p -> p.toPost() }
        }
    }

    private final inline fun <T> captureQueryTransaction(body: () -> T): T {
        return sentry.span(Thread.currentThread().stackTrace[1].methodName, "http", body)
    }

    /*
    // @SchemaMapping
    @QueryMapping
    fun getUsers(@Argument userSearch: UserSearch): List<User> {
        if (!envData.bucket.tryConsume(1))
            throw ThrottleRequestException()

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

    fun createUserRealistic(
        users: MutableList<UserNode>,
        totalDegree: Int,
        username: String,
        displayName: String
    ): MutableList<UserNode> {

        val userNode = UserNode(
            UUID.randomUUID().toString(),
            "$username@example.com",
            username,
            displayName,
            BCrypt.hashpw("12345678", BCrypt.gensalt(12)),
        )

        // Print Current User
//        println("CreateUser: ${users.size + 1}")

        // Randomly add a follow based on popularity
        if (users.size > 0) {
            println("Add Following (Degree:$totalDegree)")
            var idx = 0
            var r: Int = nextInt(totalDegree)
//            println("Starting (r:$r)")

            while (idx < users.size - 1) {
                r -= users[idx].followedBy.size
//                println("(idx: $idx | r: $r)")
                if (r <= 0.0) break
                idx++
            }
//            println("Adding User (ID:$idx)")
            val otherNode: UserNode = users[idx]
            userNode.following.add(otherNode)
            otherNode.followedBy.add(userNode)
        }

        // Randomly Create "Original" Root Posts (1-9) inclusive
        for (j in 1 until nextInt(10)) {
            val post = PostNode(
                UUID.randomUUID().toString(),
                "${userNode.displayName}'s Chrip #$j",
                "The post body can be much longer, I love cheese"
            )

            userNode.authored.add(post)
            post.authoredBy.add(userNode)
        }

        // Randomly add a reChirp based on following popularity [BROKEN: STACKOVERFLOW]
//        if (users.size > 0) {
//            println("Add Following (Degree:$totalDegree)")
//            var idx = 0
//            var r: Int = nextInt(totalDegree)
//
//            while (idx < users.size - 1) {
//                r -= users[idx].followedBy.size
//                if (r <= 0.0) break
//                idx++
//            }
//            val otherUser: UserNode = users[idx]
//
//            // Get a random post by this user
//            val otherPost: PostNode = otherUser.authored[nextInt(otherUser.authored.size)]
//
//            val post: PostNode = PostNode(
//                UUID.randomUUID().toString(),
//                "${userNode.displayName}'s re Chirp",
//                "Wow, I literally hate you"
//            )
//
//            // First set this post as "Authored" by this user
//            userNode.authored.add(post)
//            // Then set this post's parent
//            post.parentPost = otherPost
//            // Then add this post as one of the parent's children
//            otherPost.childPosts.add(post)
//        }

        users.add(userNode)
        return users
    }

    @MutationMapping
    fun createUsers(@Argument num: Int): String {
        if (!envData.bucket.tryConsume(2)) //200
            throw ThrottleRequestException()

        val csvReader = CSVReader(FileReader("names.csv"))

        val names = mutableListOf<String>()

        // we are going to read data line by line
        var nextRecord: Array<String>? = csvReader.readNext()
        while (nextRecord != null) {
            names.add(nextRecord[0])
            nextRecord = csvReader.readNext()
        }

        var users = mutableListOf<UserNode>()
//        val users = Collections.synchronizedList(mutableListOf<UserNode>()) // Thread Safe

        var totalDegree: Int = 0

        for (i in 0 until num) {
            val name1 = names[nextInt(1080)]
            val name2 = names[nextInt(1080)]
            val username = "${name1.lowercase()}-${name2.lowercase()}"
            val displayName = "${name1.uppercase()} ${name2.uppercase()}"

            println("User : ${users.size + 1} : Time : ${measureTimeMillis{ users = createUserRealistic(users, totalDegree, username, displayName) }}")
            println("-------------------------------")
            totalDegree += 2
        }

        // Begin Neo4J Session
        val session = sessionFactory.openSession()

        session.save(users)

//        println("CreateUsers T: ${(endTime - startTime) / 1_000_000.0}")

        return "Success"
    }

    @MutationMapping
    fun createUser(@Argument userCreate: UserCreate): User {
        if (!envData.bucket.tryConsume(200))
            throw ThrottleRequestException()

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
                BCrypt.hashpw(userCreate.password, BCrypt.gensalt(12)),
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
    */
}
