package app.birdsocial.birdapi.neo4j.schemas

import org.neo4j.ogm.annotation.GeneratedValue
import org.neo4j.ogm.annotation.Id
import org.neo4j.ogm.annotation.NodeEntity

@NodeEntity
abstract class Entity {
    @Id
    private val id: Long? = null
        get() = field
}
