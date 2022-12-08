package app.birdsocial.birdapi

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import io.github.cdimascio.dotenv.dotenv
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication
import java.time.Duration

@SpringBootApplication
@EnableAutoConfiguration(exclude = [
    SecurityAutoConfiguration::class,
    HibernateJpaAutoConfiguration::class
])

class BirdApiApplication {
    companion object {
        val dotenv = dotenv()

        private val limit = Bandwidth.classic(500, Refill.greedy(500, Duration.ofMinutes(15)));
        val bucket = Bucket.builder()
        .addLimit(limit)
        .build();
    }
}

fun main(args: Array<String>) {
//    val configuration: Configuration = Configuration.Builder()
//        .uri("bolt://localhost")
//        .credentials("neo4j", "password")
//        .build()

//    val sessionFactory = SessionFactory(configuration, "app.birdsocial.birdapi.domainclasses")


    runApplication<BirdApiApplication>(*args)
}
