package app.birdsocial.birdapi.services

import app.birdsocial.birdapi.exceptions.BirdException
import app.birdsocial.birdapi.graphql.types.Post
import app.birdsocial.birdapi.graphql.types.PostInput
import app.birdsocial.birdapi.graphql.types.UserInput
import app.birdsocial.birdapi.graphql.types.User
import app.birdsocial.birdapi.neo4j.schemas.PostNode
import app.birdsocial.birdapi.neo4j.schemas.UserNode
import org.mindrot.jbcrypt.BCrypt
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.cypher.query.Pagination
import org.neo4j.ogm.session.SessionFactory
import org.springframework.stereotype.Service
import java.util.*

/*
@Service
class PermissionDataService(val sessionFactory: SessionFactory) : DataService() {
    fun setupPermissions(): Boolean {
        val accountEnabled = PermissionNode("chirp.account.enabled")

        val userFollow = PermissionNode("chirp.user.follow")

        val postRead = PermissionNode("chirp.post.read")
        val postCreate = PermissionNode("chirp.post.create")
        val postCreateNewThread = PermissionNode("chirp.post.create.timeline")
        val postCreateSubPost = PermissionNode("chirp.post.create.subpost")
        val postAnnotate = PermissionNode("chirp.post.annotate")
        val postLike = PermissionNode("chirp.post.like")
        return true
    }

    fun getPost(postId: String): Post {
        return getNode<PostNode>(postId, "postId", sessionFactory).toPost()
    }

    fun createPost(authorId: String, content: String, annotation: String, parentPostId: String?): Post {
        // Begin Neo4J Session
        val session = sessionFactory.openSession()

        val post = PostNode(
            postId = UUID.randomUUID().toString(),
            body = content,
            annotation = annotation
        )

        post.authoredBy = getNode<UserNode>(authorId, "userId", sessionFactory)

        if(parentPostId != null)
            post.parentPost = getNode<PostNode>(parentPostId, "postId", sessionFactory)

        session.save(post)
        return post.toPost()
    }

    fun updatePostAnnotation(postId: String, annotation: String): Post {
        val session = sessionFactory.openSession()

        val postNode = getNode<PostNode>(postId, "postId", sessionFactory)
        postNode.annotation = annotation
        session.save(postNode)
        return postNode.toPost()
    }

    fun deletePost(postId: String) {
        val session = sessionFactory.openSession()
        val postNode = getNode<PostNode>(postId, "postId", sessionFactory)
        session.delete(postNode) // TODO - may need to delete relations first
    }
}
*/