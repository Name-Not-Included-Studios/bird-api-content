package app.birdsocial.birdapi.graphql.types

import java.util.*

data class PostInput (
    val content: String,
    val annotation: String?,
    val parentId: String?, // UUID
)
