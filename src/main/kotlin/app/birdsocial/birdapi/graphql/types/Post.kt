package app.birdsocial.birdapi.graphql.types

import app.birdsocial.birdapi.graphql.types.content.Content
import java.util.*

data class Post (
    val postId: String, // UUID
    val userId: String, // UUID
    val content: String, // TODO - Change to content
    val likesCount: Int,
    val isPublished: Boolean,
    val annotation: String?,
    val parentId: String?, // UUID
)