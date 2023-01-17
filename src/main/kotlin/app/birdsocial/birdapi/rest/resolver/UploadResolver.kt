package app.birdsocial.birdapi.rest.resolver

import app.birdsocial.birdapi.exceptions.BirdException
import app.birdsocial.birdapi.graphql.resolvers.ApiHelper
import app.birdsocial.birdapi.helper.SentryHelper
import app.birdsocial.birdapi.services.R2Service
import app.birdsocial.birdapi.services.TokenService
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayOutputStream
import java.util.*
import javax.imageio.ImageIO

@RestController
class UploadResolver(
    val request: HttpServletRequest,

    val api: ApiHelper,

    val tokenService: TokenService,
    val r2Service: R2Service,
    val sentry: SentryHelper
) {
    @PostMapping("/upload")
    fun uploadImage(@RequestParam file: MultipartFile): ResponseEntity<String> = sentry.captureTransaction {
        println("Upload")
        val tokensToConsume = if ((file.size * 0.00002).toLong() < 10) 10 else (file.size * 0.00002).toLong()
        api.throttleRequest(tokensToConsume)

        println("Tokens: $tokensToConsume")

        val userId = tokenService.authorize(request)

        println("File Size: ${file.size}")
        if (file.size >= 10000000) // 10 MB
            return ResponseEntity.status(400).body("Exceeds File Size Limit")

//        return ResponseEntity.ok("test")

        val originalImage = ImageIO.read(file.inputStream)
        val outputStream = ByteArrayOutputStream()
        val result = ImageIO.write(originalImage, "png", outputStream)
        val png = outputStream.toByteArray()

        return ResponseEntity.ok(sentry.span("S3/R2", "uploadImage") {
            r2Service.uploadImage(
                userId,
                png.size.toLong(),
                png.inputStream()
            )
        })
    }
}