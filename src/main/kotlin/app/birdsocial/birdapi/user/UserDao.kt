package app.birdsocial.birdapi.user

import java.util.*

class UserDao(private val users: List<User>) {
    fun getUsers(): List<User> {
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