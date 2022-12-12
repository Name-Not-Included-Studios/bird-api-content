package app.birdsocial.birdapi

import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import io.github.bucket4j.local.LocalBucket
import io.github.cdimascio.dotenv.dotenv
import java.time.Duration

class EnvironmentData {
    companion object {
        // ENVIRONMENT DATA
        private val dotenv = dotenv()

        fun getData(query: String): String {
            return if (System.getenv(query) != null) System.getenv(query) else dotenv[query]
        }

        // API Throttling
        private val limit = Bandwidth.classic(500, Refill.greedy(500, Duration.ofMinutes(15)));

        val bucket: LocalBucket = Bucket.builder()
            .addLimit(limit)
            .build();
    }
}