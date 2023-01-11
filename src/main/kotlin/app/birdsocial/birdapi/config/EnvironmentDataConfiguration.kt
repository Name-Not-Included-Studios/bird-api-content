package app.birdsocial.birdapi.config

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
class EnvironmentDataConfiguration {

    @Bean
    fun getEnvironmentData(): EnvironmentData {
    // ENVIRONMENT DATA
    return EnvironmentData(
        Bucket.builder()
            .addLimit(
                Bandwidth.classic(500, Refill.greedy(500, Duration.ofMinutes(15)))
            ).build()
    )}
}