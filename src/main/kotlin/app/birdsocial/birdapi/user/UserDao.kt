package app.birdsocial.birdapi.graphql.user

import app.birdsocial.birdapi.neo4j.schemas.UserGQL
import app.birdsocial.birdapi.neo4j.schemas.UserN4J

class UserDao(private val users: List<UserGQL>) {
    fun getUsers(): List<UserGQL> {
        return users
    }


//    fun getUsers(): User {
//        return User(
//            userId = UUID.randomUUID(),
//            username = username,
//            displayName = displayName,
//            bio = "Bio",
//            websiteUrl = "website",
//            avatarUrl = "avatar",
//            isVerified = false,
//            chirpCount = 42069,
//            followersCount = 69,
//            followingCount = 420
//        )
//    }
}