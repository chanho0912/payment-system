package com.noah.paymentsystem.payment.adapter.out.persistence.repository

import com.noah.paymentsystem.common.objectMapper
import com.noah.paymentsystem.payment.adapter.out.stream.util.PartitionKeyUtil
import com.noah.paymentsystem.payment.application.domain.PaymentEventMessage
import com.noah.paymentsystem.payment.application.domain.PaymentEventMessageType
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

    override fun markMessageAsSent(idempotencyKey: String, type: PaymentEventMessageType): Mono<Boolean> {
        return databaseClient.sql(
            """
                update outboxes
                set status = 'SUCCESS'
                where idempotency_key = :idempotencyKey
                and type = :type
            """.trimIndent()
        )
            .bind("idempotencyKey", idempotencyKey)
            .bind("type", type.name)
            .fetch()
            .rowsUpdated()
            .thenReturn(true)
    }

    override fun markMessageAsFailure(idempotencyKey: String, type: PaymentEventMessageType): Mono<Boolean> {
        return databaseClient.sql(
            """
                update outboxes
                set status = 'FAILURE'
                where idempotency_key = :idempotencyKey
                and type = :type
            """.trimIndent()
        )
            .bind("idempotencyKey", idempotencyKey)
            .bind("type", type.name)
            .fetch()
            .rowsUpdated()
            .thenReturn(true)
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
