package app.birdsocial.birdapi.graphql.types.user

import java.util.*

data class User(
    val userId: UUID,
    val email: String,
    val username: String,
    val displayName: String,
    val password: String,
    val bio: String = "",
    val websiteUrl: String = "",
    val avatarUrl: String = "",
    val chirpCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
)