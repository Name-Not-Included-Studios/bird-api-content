package app.birdsocial.birdapi.graphql.types.user

data class UserCreate (
    val username: String,
    val displayName: String,
    val password: String,
    val email: String,
)