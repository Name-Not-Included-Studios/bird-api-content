package app.birdsocial.birdapi.graphql.types

import app.birdsocial.birdapi.graphql.types.content.Content
import java.util.*

data class Post (
    val postId: String, // UUID
    val author: LazyUser, // UUID
    val content: String, // TODO - Change to content
    val annotation: String,
    val parent: LazyPost?, // UUID
    val likedBy: List<LazyUser>,
)