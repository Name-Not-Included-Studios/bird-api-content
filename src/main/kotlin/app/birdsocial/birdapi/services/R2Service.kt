package app.birdsocial.birdapi.services

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.ObjectMetadata
import org.springframework.stereotype.Service
import java.io.InputStream
import java.util.*


@Service
class R2Service(
    val r2Client: AmazonS3
) {
    fun uploadImage(userId: String, size: Long, file: InputStream): String {
        val postId = UUID.randomUUID().toString()

        val metadata = ObjectMetadata()
        metadata.contentLength = size
        metadata.userMetadata.put("test", "data")
        r2Client.putObject("images/$userId", "$postId.png", file, metadata)
        return postId
    }
}