package app.birdsocial.birdapi.services

import org.neo4j.ogm.session.SessionFactory
import org.springframework.stereotype.Service

@Service
class DataService(val sessionFactory: SessionFactory) {}