package app.birdsocial.birdapi.services

import app.birdsocial.birdapi.graphql.types.Post
import app.birdsocial.birdapi.helper.SentryHelper
import app.birdsocial.birdapi.neo4j.schemas.PostNode
import app.birdsocial.birdapi.neo4j.schemas.UserNode
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.cypher.Filters
import org.neo4j.ogm.cypher.query.Pagination
import org.neo4j.ogm.session.SessionFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class PostDataService(
    val sessionFactory: SessionFactory,
    val sentryHelper: SentryHelper,
    ) : DataService() {
    fun getPost(postId: String): Post {
        return sentryHelper.span("neo4j", "getNode") { getNode<PostNode>(postId, "postId", sessionFactory).toPost() }
    }

    fun getPost(filter: Filter, pagination: Pagination = Pagination(0, 25)): Post {
        return getPosts(filter, pagination)[0]
    }

    fun getPost(filters: Filters, pagination: Pagination = Pagination(0, 25)): Post {
        return getPosts(filters, pagination)[0]
    }

    fun getPosts(filter: Filter, pagination: Pagination = Pagination(0, 25)): List<Post> {
        val session = sessionFactory.openSession()
        return sentryHelper.span("neo4j", "loadAll") { session.loadAll(PostNode::class.java, filter, pagination).map { postNode -> postNode.toPost() } }
    }

    fun getPosts(filters: Filters, pagination: Pagination = Pagination(0, 25)): List<Post> {
        val session = sessionFactory.openSession()
        return sentryHelper.span("neo4j", "loadAll") { session.loadAll(PostNode::class.java, filters, pagination).map { postNode -> postNode.toPost() } }
    }

    fun createPost(authorId: String, content: String, annotation: String, parentPostId: String?): Post {
        // Begin Neo4J Session
        val session = sessionFactory.openSession()

        val post = PostNode(
            postId = UUID.randomUUID().toString(),
            content = content,
            annotation = annotation
        )

        post.authoredBy = sentryHelper.span("neo4j", "getNode") { getNode<UserNode>(authorId, "userId", sessionFactory) }

        if(parentPostId != null)
            post.parentPost = sentryHelper.span("neo4j", "getNode") { getNode<PostNode>(parentPostId, "postId", sessionFactory) }

        sentryHelper.span("neo4j", "save") { session.save(post) }
        return post.toPost()
    }

    fun updatePostAnnotation(postId: String, annotation: String): Post {
        val session = sessionFactory.openSession()

        val postNode = sentryHelper.span("neo4j", "getNode") { getNode<PostNode>(postId, "postId", sessionFactory) }
        postNode.annotation = annotation
        sentryHelper.span("neo4j", "save") { session.save(postNode) }
        return postNode.toPost()
    }

    fun deletePost(postId: String) {
        val session = sessionFactory.openSession()
        val postNode = sentryHelper.span("neo4j", "getNode") { getNode<PostNode>(postId, "postId", sessionFactory) }
        sentryHelper.span("neo4j", "delete") { session.delete(postNode) } // TODO - may need to delete relations first
    }
}