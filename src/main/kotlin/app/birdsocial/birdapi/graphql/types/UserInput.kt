package app.birdsocial.birdapi.graphql.types

data class UserInput (
    val username: String?,
    val displayName: String?,
    val bio: String?,
    val websiteUrl: String?,
    val avatarUrl: String?,
)
