package app.birdsocial.birdapi.services

import app.birdsocial.birdapi.exceptions.BirdException
import org.neo4j.ogm.cypher.ComparisonOperator
import org.neo4j.ogm.cypher.Filter
import org.neo4j.ogm.cypher.query.Pagination
import org.neo4j.ogm.session.SessionFactory

open class DataService {
    // Intellij Magic - inline and reified????
    inline fun <reified T> getNode(value: String, paramName: String, sessionFactory: SessionFactory): T { //, nodeType: KClass<T>
        val session = sessionFactory.openSession()

//        println("getNode<${T::class.simpleName}>($value, $paramName)")

        val filter = Filter(paramName, ComparisonOperator.EQUALS, value)
        val nodes: List<T> = session.loadAll(T::class.java, filter, Pagination(0, 5)).toList()

        if (nodes.size > 1)
            throw BirdException("Server Error: Multiple Nodes Returned")

        if (nodes.isEmpty())
            throw BirdException("Server Error: No Nodes Returned")

        return nodes[0]
    }

    inline fun <reified T> getNodes(value: String, param: String, sessionFactory: SessionFactory, page: Int = 0, pageSize: Int = 25): List<T> { //, nodeType: KClass<T>
        val session = sessionFactory.openSession()

        val filter = Filter(param, ComparisonOperator.EQUALS, value)
        return session.loadAll(T::class.java, filter, Pagination(page, pageSize)).toList()
    }
}