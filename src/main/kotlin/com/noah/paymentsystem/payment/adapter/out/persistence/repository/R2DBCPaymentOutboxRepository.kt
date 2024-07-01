package com.noah.paymentsystem.payment.adapter.out.persistence.repository

import com.noah.paymentsystem.common.objectMapper
import com.noah.paymentsystem.payment.adapter.out.stream.util.PartitionKeyUtil
import com.noah.paymentsystem.payment.application.domain.PaymentEventMessage
import com.noah.paymentsystem.payment.application.domain.PaymentEventMessageType.PAYMENT_CONFIRMATION_SUCCESS
import com.noah.paymentsystem.payment.application.domain.PaymentStatus
import com.noah.paymentsystem.payment.application.port.out.PaymentStatusUpdateCommand
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class R2DBCPaymentOutboxRepository(
    private val databaseClient: DatabaseClient,
    private val partitionKeyUtil: PartitionKeyUtil
) : PaymentOutboxRepository {
    override fun insertOutbox(command: PaymentStatusUpdateCommand): Mono<PaymentEventMessage> {
        require(command.status == PaymentStatus.SUCCESS)
        val paymentEventMessage = createPaymentEventMessage(command)

        return databaseClient.sql(
            INSERT_OUTBOX_QUERY
        )
            .bind("idempotencyKey", paymentEventMessage.payload["orderId"] as String)
            .bind("type", paymentEventMessage.type.name)
            .bind("partitionKey", paymentEventMessage.metadata["partitionKey"] ?: 0)
            .bind("payload", objectMapper.writeValueAsString(paymentEventMessage.payload))
            .bind("metadata", objectMapper.writeValueAsString(paymentEventMessage.metadata))
            .fetch()
            .rowsUpdated()
            .thenReturn(paymentEventMessage)
    }

    private fun createPaymentEventMessage(command: PaymentStatusUpdateCommand) =
        PaymentEventMessage(
            type = PAYMENT_CONFIRMATION_SUCCESS,
            payload = mapOf(
                "orderId" to command.orderId,
            ),
            metadata = mapOf(
                "partitionKey" to partitionKeyUtil.createPartitionKey(command.orderId.hashCode())
            )
        )

    companion object {
        val INSERT_OUTBOX_QUERY =
            """
                INSERT INTO outboxes (idempotency_key, type, partition_key, payload, metadata)
                VALUES (:idempotencyKey, :type, :partitionKey, :payload, :metadata)
            """.trimIndent()
    }
}
