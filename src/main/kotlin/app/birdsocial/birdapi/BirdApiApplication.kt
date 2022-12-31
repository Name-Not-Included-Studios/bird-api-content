package app.birdsocial.birdapi

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

class BirdApiApplication {}

fun main(args: Array<String>) {
    runApplication<BirdApiApplication>(*args)
}
