package app.birdsocial.birdapi.services

import app.birdsocial.birdapi.config.RabbitMQConfig
import org.springframework.amqp.core.MessageBuilder
import org.springframework.amqp.rabbit.core.RabbitTemplate
import org.springframework.stereotype.Service

@Service
class PermissionService(
    val rabbitTemplate: RabbitTemplate,
    val rabbitMQConfig: RabbitMQConfig,
) {
    fun checkPermission(userId: String, perm: String): Boolean {
        val message = MessageBuilder.withBody("$userId|$perm".toByteArray()).build()

        val result = rabbitTemplate.sendAndReceive("", "", message)
        var response = ""
        if (result != null) {
            val correlationId: String = message.messageProperties.correlationId
            println("correlationId: $correlationId")

            val headers = result.messageProperties.headers as HashMap<String, Any>
            val msgId = headers["spring_returned_message_correlation"] as String?

            if (msgId == correlationId) {
                response = result.body.toString()
                println("client receive： $response")
            }
        }
        return false
    }
}