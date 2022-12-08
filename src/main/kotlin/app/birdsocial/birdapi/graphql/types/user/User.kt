package app.birdsocial.birdapi.graphql.schemas.user

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
    val isVerified: Boolean = false,
    val chirpCount: Int = 0,
    val followersCount: Int = 0,
    val followingCount: Int = 0,
)