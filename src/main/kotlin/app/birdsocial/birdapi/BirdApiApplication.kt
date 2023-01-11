package app.birdsocial.birdapi

import io.sentry.Sentry
import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration
import org.springframework.boot.runApplication
import java.time.Duration

@SpringBootApplication
@EnableAutoConfiguration(exclude = [
//    SecurityAutoConfiguration::class,
    HibernateJpaAutoConfiguration::class
])

class BirdApiApplication {}

fun main(args: Array<String>) {
//    Sentry.init { options ->
//        options.dsn = "https://2dce25466378481d83e3762fcd44f17e@o4504471975297024.ingest.sentry.io/4504471977197568"
//        // Set tracesSampleRate to 1.0 to capture 100% of transactions for performance monitoring.
//        // We recommend adjusting this value in production.
//        options.tracesSampleRate = 1.0
//        // When first trying Sentry it's good to see what the SDK is doing:
//        options.isDebug = true
//        options.environment = "development"
//    }

    runApplication<BirdApiApplication>(*args)
}
