package app.birdsocial.birdapi.graphql.types

data class AuthInput (
    val email: String,
    val password: String,
)