package app.birdsocial.birdapi.graphql.types

import java.util.*

data class User(
    val userId: String, // UUID
    val username: String,
    val displayName: String,
    val bio: String,
    val websiteUrl: String,
    val avatarUrl: String,
    val chirpCount: Int,
    val followersCount: Int,
    val followingCount: Int,
)