package app.birdsocial.birdapi.graphql.resolvers

import app.birdsocial.birdapi.config.ApplicationConfig
import app.birdsocial.birdapi.exceptions.AuthException
import app.birdsocial.birdapi.exceptions.BirdException
import app.birdsocial.birdapi.graphql.types.AuthInput
import app.birdsocial.birdapi.graphql.types.Pagination
import app.birdsocial.birdapi.graphql.types.User
import app.birdsocial.birdapi.graphql.types.UserInput
import app.birdsocial.birdapi.helper.SentryHelper
import app.birdsocial.birdapi.repository.PostRepository
import app.birdsocial.birdapi.repository.PostService
import app.birdsocial.birdapi.repository.UserRepository
import app.birdsocial.birdapi.repository.UserService
import app.birdsocial.birdapi.services.AuthService
import app.birdsocial.birdapi.services.TokenService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.env.Environment
import org.springframework.data.neo4j.core.Neo4jTemplate
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.MutationMapping
import org.springframework.graphql.data.method.annotation.QueryMapping

class UserResolver(
    val request: HttpServletRequest,
    val env: Environment,

    val appConfig: ApplicationConfig,

    val userRepository: UserRepository,
    val postRepository: PostRepository,
    val userService: UserService,
    val postService: PostService,
    val neo4j: Neo4jTemplate,

    val sentry: SentryHelper,

    val api: ApiHelper,
    val authService: AuthService,
    val tokenService: TokenService,
) {

    @QueryMapping
    fun getMe(): User = sentry.captureTransaction {
        // Check if too many requests
        api.throttleRequest(1)

        // Get userId from authorization token
        val userId = tokenService.authorize(request)

        val user = userService.findOneById(userId)

        println("Posts: ${user.posts}")

        // Get User from Database
        return user.toUser()
    }

    // deleteUser() requires access token as well as email and password
    @MutationMapping
    fun deleteUser(@Argument auth: AuthInput): Boolean =
        // Check if too many requests
        sentry.captureTransaction { // TODO - Leaves us with nothing to track down bad actors
            api.throttleRequest(350)

            // Get userId from authorization token
            val userId = tokenService.authorize(request)

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
    fun followUser(
        @Argument followerId: String
    ): User {//= sentry.captureQueryTransaction {
        // Check if too many requests
        api.throttleRequest(1)

        // Get userId from authorization token
        val userId = tokenService.authorize(request)

        // Get me from database
        val user = userService.findOneById(userId)

        // Get followee from database
        val followee = userService.findOneById(followerId)

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
    ): User = sentry.captureTransaction {
        // Check if too many requests
        api.throttleRequest(1)

        // Get userId from authorization token
        val userId = tokenService.authorize(request)

        // Get me from database
        val user = userService.findOneById(userId)

        // Get followee from database
        val followee = userService.findOneById(followerId)

        // Remove followee from my following list
        user.following.remove(followee)
//        followee.followedBy.remove(user)

        // Save changes to database
        userRepository.save(user)
//        userRepository.save(followee)

        return followee.toUser()
    }

    @MutationMapping
    fun updateUser(
        @Argument user: UserInput
    ): User = sentry.captureTransaction {
        // Check if too many requests
        api.throttleRequest(5)

        // Get userId from authorization token
        val userId = tokenService.authorize(request)

        println("UserID: $userId")

        // Get me from database
        val userNode = userService.findOneById(userId)

        // If user supplied a new username
        if (user.username != null) {
            // Find any user in database with the username supplied
            if (userService.findAllByParam("username", user.username, Pagination(0, 1)).isNotEmpty())
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

        // Save changes to database
        userRepository.save(userNode)

        return userNode.toUser()
    }
}