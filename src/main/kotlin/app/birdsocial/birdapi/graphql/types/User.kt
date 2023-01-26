package app.birdsocial.birdapi.graphql.types

data class User(
    val userId: String, // UUID
    val username: String,
    val displayName: String,
    val bio: String,
    val websiteUrl: String,
    val chirps: List<LazyPost>,
    val followers: List<LazyUser>,
    val following: List<LazyUser>,
)