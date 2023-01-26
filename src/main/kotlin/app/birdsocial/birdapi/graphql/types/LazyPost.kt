package app.birdsocial.birdapi.graphql.types

data class LazyPost (
    val postId: String, // UUID
    val content: String, // TODO - Change to content
    val annotation: String,
)