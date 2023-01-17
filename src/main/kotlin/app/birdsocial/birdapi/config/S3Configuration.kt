package app.birdsocial.birdapi.config

import app.birdsocial.birdapi.exceptions.BirdException
import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.env.Environment
import org.springframework.core.env.get

@Configuration
class S3Configuration(
    val env: Environment
) {
//    @Bean
//    fun s3(): AmazonS3 {
//        val awsCredentials: AWSCredentials = BasicAWSCredentials("accessKey", "secretKey")
//        return AmazonS3ClientBuilder
//            .standard()
//            .withRegion("ap-south-1")
//            .withCredentials(AWSStaticCredentialsProvider(awsCredentials))
//            .build()
//    }

    @Bean
    fun s3client(
//        @Value("\${r2.endpoint}") r2ServiceEndpoint: String?,
//        @Value("\${r2.accountId}") accountIdValue: String?,
//        @Value("\${r2.accessKey}") accessKeyValue: String?,
//        @Value("\${r2.secretKey}") secretKeyValue: String?
    ): AmazonS3 {
//        val accountR2Url = String.format(
//            env["R2_ENDPOINT"] ?: throw BirdException("No R2_ENDPOINT configured"),
//            env["R2_ACCOUNT_ID"]
//        )

        val accountR2Url = env["R2_URL"] ?: throw BirdException("No R2_URL configured")
        val credentials: AWSCredentials = BasicAWSCredentials(
            env["R2_BUCKET_ID"],
            env["R2_SECRET"]
        )
        val endpointConfiguration: AwsClientBuilder.EndpointConfiguration =
            AwsClientBuilder.EndpointConfiguration(accountR2Url, null)
        return AmazonS3ClientBuilder
            .standard()
            .withEndpointConfiguration(endpointConfiguration)
            .withPathStyleAccessEnabled(true)
            .withCredentials(AWSStaticCredentialsProvider(credentials))
            .build()
    }
}