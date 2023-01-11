package app.birdsocial.birdapi.config

import app.birdsocial.birdapi.exceptions.BirdException
import io.github.bucket4j.Bandwidth
import io.github.bucket4j.Bucket
import io.github.bucket4j.Refill
import io.github.bucket4j.local.LocalBucket
import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component
import java.time.Duration

//@Configuration
class EnvironmentData(
    // Rate Limit Bucket
    val bucket: LocalBucket
//    = Bucket.builder()
//            .addLimit(
//                Bandwidth.classic(500, Refill.greedy(500, Duration.ofMinutes(15)))
//            ).build()
) {

    // Don't use `dotenv` by itself, please use `getData()`
    val dotenv: Dotenv = dotenv()


    fun getData(query: String): String {
        return System.getenv(query) ?: dotenv[query]
    }

    final inline fun <reified T> getData(query: String): T {
//        return if (System.getenv(query) != null) System.getenv(query) else dotenv[query]
        return when (T::class) {
            String::class -> (System.getenv(query) ?: dotenv[query]) as T
            Int::class -> (System.getenv(query) ?: dotenv[query]).toInt() as T
            else -> throw BirdException("Unknown Generic Type")
        }
    }
}

//class EnvironmentData {
//    companion object {
//        // ENVIRONMENT DATA
//        private val dotenv = dotenv()
//
//        fun getData(query: String): String {
//            return if (System.getenv(query) != null) System.getenv(query) else dotenv[query]
//        }
//
//        // API Throttling
//        private val limit = Bandwidth.classic(500, Refill.greedy(500, Duration.ofMinutes(15)));
//
//        val bucket: LocalBucket = Bucket.builder()
//            .addLimit(limit)
//            .build();
//    }
//}