package app.birdsocial.birdapi.graphql

import app.birdsocial.birdapi.config.EnvironmentData
import app.birdsocial.birdapi.exceptions.AuthException
import app.birdsocial.birdapi.exceptions.BirdException
import app.birdsocial.birdapi.exceptions.ThrottleRequestException
import app.birdsocial.birdapi.graphql.types.*
import app.birdsocial.birdapi.helper.SentryHelper
import app.birdsocial.birdapi.services.AuthService
import app.birdsocial.birdapi.services.PostDataService
import app.birdsocial.birdapi.services.TokenService
import app.birdsocial.birdapi.services.UserDataService
//import io.sentry.spring.tracing.SentrySpan
//import io.sentry.spring.tracing.SentryTransaction
import jakarta.servlet.http.HttpServletRequest
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.cypher.query.Pagination
import org.passay.*
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Controller
import java.util.*


@Controller
class ApiGateway(
    val request: HttpServletRequest,
    val envData: EnvironmentData,
    val sentryHelper: SentryHelper,

    val authService: AuthService,
    val tokenService: TokenService,
    val userDataService: UserDataService,
    val postDataService: PostDataService,
//    val permissionDataService: PermissionDataService,
) {

    @QueryMapping
    fun apiVersion(): String = captureQueryTransaction {
        return@captureQueryTransaction "1.0"
    }

    @QueryMapping
    fun searchUsers(@Argument query: UserSearchCriteria): List<User> = captureQueryTransaction {
        if (!envData.bucket.tryConsume(1))
            throw ThrottleRequestException()

        val usernameContainsFilter = Filter("username", ComparisonOperator.CONTAINING, query.usernameContains)
        val usernameStartsWithFilter =
            Filter("username", ComparisonOperator.STARTING_WITH, query.usernameStartsWith)
        val usernameEndsWithFilter = Filter("username", ComparisonOperator.ENDING_WITH, query.usernameEndsWith)
        val usernameFollowerCountGreaterThanFilter =
            Filter("followersCount", ComparisonOperator.GREATER_THAN_EQUAL, query.followerCountGreaterThan)
        val usernameFollowerCountLessThanFilter =
            Filter("followersCount", ComparisonOperator.LESS_THAN_EQUAL, query.followerCountLessThan)
        val bioContainsFilter = Filter("bio", ComparisonOperator.CONTAINING, query.bioContains)
        val usernameFuzzyFilter =
            Filter("username", ComparisonOperator.LIKE, query.usernameFuzzySearch) // TODO - Change to Fuzzy Search

        val filter = usernameContainsFilter.and(usernameStartsWithFilter)
            .and(usernameEndsWithFilter).and(usernameFollowerCountGreaterThanFilter)
            .and(usernameFollowerCountLessThanFilter).and(bioContainsFilter).and(usernameFuzzyFilter)

        return@captureQueryTransaction sentryHelper.span("user-srv", "getUsers") { userDataService.getUsers(filter) }
    }

    @QueryMapping
    fun getMe(): User = captureQueryTransaction {
        println("Tokens: ${envData.bucket.availableTokens}")
        if (!envData.bucket.tryConsume(1))
            throw ThrottleRequestException()

        val access = request.getHeader(HttpHeaders.AUTHORIZATION) ?: throw AuthException()
        val token = sentryHelper.span("tkn-srv", "getToken") { tokenService.getToken(access, false) }
        println("Query - getMe() - Payload: ${token.audience[0]}")

        return@captureQueryTransaction sentryHelper.span(
            "user-srv",
            "getUser"
        ) { userDataService.getUser(token.audience[0]) }
    }

    // TODO - Fix t ck
    @QueryMapping
    fun getAccessToken(): String = captureQueryTransaction {
        println("Tokens: ${envData.bucket.availableTokens}")

        // Check if too many requests
        if (!envData.bucket.tryConsume(50))
            throw ThrottleRequestException()

        // User Sent Request
        val refresh = request.getHeader(HttpHeaders.AUTHORIZATION) ?: throw AuthException()
        // Get User ID from refresh token (also checks if signature is valid)
        val userId = sentryHelper.span("tkn-srv", "getToken") { tokenService.getToken(refresh, true).audience[0] }
        // Get DB Refresh Token
        val storedRefresh = sentryHelper.span("user-srv", "getRefreshToken") { userDataService.getRefreshToken(userId) }

        if (refresh != storedRefresh)
            throw AuthException()

        return@captureQueryTransaction sentryHelper.span(
            "tkn-srv",
            "createAccessToken"
        ) { tokenService.createAccessToken(refresh) }
    }

    @MutationMapping
    fun login(@Argument auth: AuthInput): LoginResponse = captureQueryTransaction {
        if (!envData.bucket.tryConsume(50))
            throw ThrottleRequestException()

        sentryHelper.span("compute", "checkPasswordValidity") { checkPasswordValidity(auth.password) }

        return@captureQueryTransaction sentryHelper.span("auth-srv", "login") { authService.login(auth) }
    }

    @MutationMapping
    fun createAccount(@Argument auth: AuthInput): LoginResponse = captureQueryTransaction {
        if (!envData.bucket.tryConsume(200))
            throw ThrottleRequestException()

        sentryHelper.span("compute", "checkPasswordValidity") { checkPasswordValidity(auth.password) }

        if (sentryHelper.span("user-srv", "getUsers") {
                userDataService.getUsers(
                    Filter(
                        "email",
                        ComparisonOperator.EQUALS,
                        auth.email
                    )
                ).isNotEmpty()
            })
            throw BirdException("Email Already in Use")

        val userId = UUID.randomUUID().toString()
        val refresh = tokenService.createRefreshToken(userId)

        return@captureQueryTransaction LoginResponse(
            sentryHelper.span("user-srv", "createUser") {
                userDataService.createUser(
                    userId,
                    auth.email,
                    auth.password
                )
            },
            sentryHelper.span("tkn-srv", "createAccessToken") { tokenService.createAccessToken(refresh) },
            refresh
        )
    }

    @MutationMapping
    fun updatePassword(
        @Argument oldPassword: String,
        @Argument newPassword: String
    ): LoginResponse = captureQueryTransaction {

        if (!envData.bucket.tryConsume(200))
            throw ThrottleRequestException()

        sentryHelper.span("compute", "checkPasswordValidity") { checkPasswordValidity(newPassword) }

        val access = request.getHeader(HttpHeaders.AUTHORIZATION) ?: throw AuthException()

        return@captureQueryTransaction sentryHelper.span("auth-srv", "updatePassword") {
            authService.updatePassword(
                oldPassword,
                newPassword,
                access
            )
        }
    }

    @MutationMapping
    fun deleteUser(): User = captureQueryTransaction {
        if (!envData.bucket.tryConsume(350))
            throw ThrottleRequestException()

        val access = request.getHeader(HttpHeaders.AUTHORIZATION) ?: throw AuthException()
        val userId = tokenService.getToken(access, false).audience[0]
        return@captureQueryTransaction sentryHelper.span(
            "user-srv",
            "deleteUser"
        ) { userDataService.deleteUser(userId) } // TODO - Should require password?
    }

    @MutationMapping
    fun createPost(
        @Argument post: PostInput
    ): Post = captureQueryTransaction {
        if (!envData.bucket.tryConsume(10)) // TODO - Add tokens for image size
            throw ThrottleRequestException()

        val access = request.getHeader(HttpHeaders.AUTHORIZATION) ?: throw AuthException()
        val userId = sentryHelper.span("tkn-srv", "getToken") { tokenService.getToken(access, false).audience[0] }
        return@captureQueryTransaction sentryHelper.span("post-srv", "createPost") {
            postDataService.createPost(
                userId,
                post.content,
                post.annotation ?: "",
                post.parentId
            )
        }
    }

    @MutationMapping
    fun deletePost(
        @Argument postId: String
    ): Post = captureQueryTransaction {
        if (!envData.bucket.tryConsume(5))
            throw ThrottleRequestException()

        val access = request.getHeader(HttpHeaders.AUTHORIZATION) ?: throw AuthException()
        val userId = tokenService.getToken(access, false).audience[0]
        val post = sentryHelper.span("post-srv", "getPost") { postDataService.getPost(postId) }

        if (post.userId == userId)
            sentryHelper.span("post-srv", "deletePost") { postDataService.deletePost(postId) }

        return@captureQueryTransaction post
    }

    @MutationMapping
    fun annotatePost(
        @Argument postId: String,
        @Argument annotation: String
    ): Post = captureQueryTransaction {
        if (!envData.bucket.tryConsume(5))
            throw ThrottleRequestException()

        val access = request.getHeader(HttpHeaders.AUTHORIZATION) ?: throw AuthException()
        if (sentryHelper.span("tkn-srv", "checkToken") { tokenService.checkToken(access, false) })
            throw AuthException()

        return@captureQueryTransaction sentryHelper.span(
            "post-srv",
            "updatePostAnnotation"
        ) { postDataService.updatePostAnnotation(postId, annotation) }
    }

    @MutationMapping
    fun followUser(
        @Argument followerId: String
    ): User = captureQueryTransaction {
        if (!envData.bucket.tryConsume(1))
            throw ThrottleRequestException()

        val access = request.getHeader(HttpHeaders.AUTHORIZATION) ?: throw AuthException()
        val userId = sentryHelper.span("tkn-srv", "getToken") { tokenService.getToken(access, false).audience[0] }
        return@captureQueryTransaction sentryHelper.span("post-srv", "setFollowUser") {
            userDataService.setFollowUser(
                userId,
                followerId,
                true
            )
        }
    }

    @MutationMapping
    fun unfollowUser(
        @Argument followerId: String
    ): User = captureQueryTransaction {
        if (!envData.bucket.tryConsume(1))
            throw ThrottleRequestException()

        val access = request.getHeader(HttpHeaders.AUTHORIZATION) ?: throw AuthException()
        val userId = sentryHelper.span("tkn-srv", "getToken") { tokenService.getToken(access, false).audience[0] }
        return@captureQueryTransaction sentryHelper.span("user-srv", "setFollowUser") {
            userDataService.setFollowUser(
                userId,
                followerId,
                false
            )
        }
    }

    @MutationMapping
    fun likePost(
        @Argument postId: String
    ): Post = captureQueryTransaction {
        if (!envData.bucket.tryConsume(1))
            throw ThrottleRequestException()

        val access = request.getHeader(HttpHeaders.AUTHORIZATION) ?: throw AuthException()
        val userId = sentryHelper.span("tkn-srv", "getToken") { tokenService.getToken(access, false).audience[0] }
        return@captureQueryTransaction sentryHelper.span("user-srv", "setLikedPost") {
            userDataService.setLikedPost(
                userId,
                postId,
                true
            )
        }
    }

    @MutationMapping
    fun unlikePost(
        @Argument postId: String
    ): Post = captureQueryTransaction {
        if (!envData.bucket.tryConsume(1))
            throw ThrottleRequestException()

        val access = request.getHeader(HttpHeaders.AUTHORIZATION) ?: throw AuthException()
        val userId = sentryHelper.span("tkn-srv", "getToken") { tokenService.getToken(access, false).audience[0] }
        return@captureQueryTransaction sentryHelper.span("user-srv", "setLikedPost") {
            userDataService.setLikedPost(
                userId,
                postId,
                false
            )
        }
    }

    fun checkPasswordValidity(password: String): Boolean {
        val validator = sentryHelper.span("compute", "createPasswordValidator") {
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

        val passwordResult = sentryHelper.span("compute", "validate") { validator.validate(PasswordData(password)) }

        if (!passwordResult.isValid)
            throw BirdException("Password Not Valid: ${validator.getMessages(passwordResult)}")

        return true
    }

    @QueryMapping
    fun getPost(
        @Argument postId: String
    ): Post = captureQueryTransaction {
        if (!envData.bucket.tryConsume(1))
            throw ThrottleRequestException()

        return@captureQueryTransaction sentryHelper.span("post-srv", "getPost") { postDataService.getPost(postId) }
    }

    @MutationMapping
    fun updateUser(
        @Argument user: UserInput
    ): User = captureQueryTransaction {
        if (!envData.bucket.tryConsume(5))
            throw ThrottleRequestException()

        val access = request.getHeader(HttpHeaders.AUTHORIZATION) ?: throw AuthException()
        val token = tokenService.getToken(access, false)
        val userID = token.audience[0]

        return@captureQueryTransaction sentryHelper.span("user-srv", "updateUser") {
            userDataService.updateUser(
                userID,
                user
            )
        }
    }

    //    @SentryTransaction(operation = "operation-name")
//    @SentrySpan(operation = "task-name")
    @QueryMapping
    fun getRecentPostsFromUser(
        @Argument userId: String,
        @Argument page: Int,
        @Argument pageSize: Int
    ): List<Post> = captureQueryTransaction {
        if (!envData.bucket.tryConsume(5))
            throw ThrottleRequestException()

        val pageSizeLimited = if (pageSize > 25) 25 else pageSize

        return sentryHelper.span("user-srv", "getUserPosts") {
            userDataService.getUserPosts(
                userId,
                page,
                pageSizeLimited
            )
        }
        // return captureTransactionDatabase { userDataService.getUserPosts(userId, page, pageSizeLimited) }
    }

    @QueryMapping
    fun getTimeline(
        @Argument page: Int,
        @Argument pageSize: Int,
    ): List<Post> = captureQueryTransaction {
        if (!envData.bucket.tryConsume(5))
            throw ThrottleRequestException()

        val pageSizeLimited = if (pageSize > 25) 25 else pageSize

        val access = request.getHeader(HttpHeaders.AUTHORIZATION) ?: throw AuthException()
        val token = tokenService.getToken(access, false)
        val userID = token.audience[0]

        return sentryHelper.span("user-srv", "getFollowedPosts") {
            userDataService.getFollowedPosts(userID, page, pageSizeLimited)
        }
    }

    private final inline fun <T> captureQueryTransaction(body: () -> T): T {
        return sentryHelper.span(Thread.currentThread().stackTrace[1].methodName, "http", body)
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
