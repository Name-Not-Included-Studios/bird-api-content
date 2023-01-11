package app.birdsocial.birdapi.neo4j.schemas

import app.birdsocial.birdapi.graphql.types.User
import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import java.util.*
import org.neo4j.ogm.annotation.NodeEntity
import org.neo4j.ogm.annotation.Property
import org.neo4j.ogm.annotation.Relationship
import java.time.LocalDateTime

/*
@NodeEntity(label = "Permission")
data class PermissionNode (
        var name: String = "",
) {
        @Id @GeneratedValue
        var id: Long? = null

        @Relationship("HAS_PERMISSION", direction = Relationship.Direction.OUTGOING)
        var childPermissions: MutableList<PermissionNode> = mutableListOf()

        @Relationship("HAS_PERMISSION", direction = Relationship.Direction.INCOMING)
        var parentPermissions: MutableList<PermissionNode> = mutableListOf()

        @Relationship("HAS_PERMISSION", direction = Relationship.Direction.INCOMING)
        var parentUserPermissions: MutableList<UserNode> = mutableListOf()
}
*/