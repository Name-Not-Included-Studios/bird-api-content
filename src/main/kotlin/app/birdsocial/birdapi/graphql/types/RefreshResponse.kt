package app.birdsocial.birdapi.graphql.types

data class RefreshResponse(
    val access_token: String,
    val accessExpiry: String,
)