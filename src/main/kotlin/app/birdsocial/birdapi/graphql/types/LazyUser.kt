package app.birdsocial.birdapi.graphql.types

data class LazyUser(
    val userId: String, // UUID
    val username: String,
    val displayName: String,
    val bio: String,
    val websiteUrl: String,
)