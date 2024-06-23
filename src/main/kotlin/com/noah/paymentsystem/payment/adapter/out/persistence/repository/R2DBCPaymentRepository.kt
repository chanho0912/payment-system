package com.noah.paymentsystem.payment.adapter.out.persistence.repository

import com.noah.paymentsystem.payment.adapter.out.persistence.util.MySQLDateTimeFormatter
import com.noah.paymentsystem.payment.application.domain.PaymentEvent
import com.noah.paymentsystem.payment.application.domain.PaymentStatus
import com.noah.paymentsystem.payment.application.domain.PendingPaymentEvent
import com.noah.paymentsystem.payment.application.domain.PendingPaymentOrder
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.math.BigDecimal
import java.math.BigInteger
import java.time.LocalDateTime

@Repository
class R2DBCPaymentRepository(
    private val databaseClient: DatabaseClient,
    private val transactionalOperator: TransactionalOperator
) : PaymentRepository {

    override fun save(paymentEvent: PaymentEvent): Mono<Void> {
        return insertPaymentEvent(paymentEvent)
            .flatMap { selectPaymentEventId() }
            .flatMap { paymentEventId -> insertPaymentOrders(paymentEvent, paymentEventId) }
            .`as`(transactionalOperator::transactional)
            .then()
    }

    override fun getPendingPayment(): Flux<PendingPaymentEvent> {
        return databaseClient.sql(
            """
                SELECT pe.id as payment_event_id, pe.payment_key, pe.order_id, po.id as payment_order_id, po.payment_order_status, po.amount, po.failed_count, po.threshold
                FROM payment_events pe
                INNER JOIN payment_orders po ON pe.id = po.payment_event_id
                WHERE (po.payment_order_status = 'UNKNOWN' OR (po.payment_order_status = 'EXECUTING' AND po.updated_at <= :updatedAt - INTERVAL 3 MINUTE))
                AND po.failed_count < po.threshold
                LIMIT 10
            """.trimIndent()
        )
            .bind("updatedAt", LocalDateTime.now().format(MySQLDateTimeFormatter))
            .fetch()
            .all()
            .groupBy { it["payment_event_id"] as Long }
            .flatMap { groupedFlux ->
                groupedFlux.collectList().map { result ->
                    PendingPaymentEvent(
                        paymentEventId = groupedFlux.key(),
                        paymentKey = result.first()["payment_key"] as String,
                        orderId = result.first()["order_id"] as String,
                        pendingPaymentOrders = result.map { paymentOrder ->
                            PendingPaymentOrder(
                                paymentOrderId = paymentOrder["payment_order_id"] as Long,
                                status = PaymentStatus.of(paymentOrder["payment_order_status"] as String),
                                amount = (paymentOrder["amount"] as BigDecimal).toLong(),
                                failedCount = paymentOrder["failed_count"] as Byte,
                                threshold = paymentOrder["threshold"] as Byte
                            )
                        }
                    )
                }
            }
    }

    private fun insertPaymentEvent(paymentEvent: PaymentEvent): Mono<Long> {
        return databaseClient.sql(INSERT_PAYMENT_EVENT_QUERY)
            .bind("buyerId", paymentEvent.buyerId)
            .bind("orderName", paymentEvent.orderName)
            .bind("orderId", paymentEvent.orderId)
            .fetch()
            .rowsUpdated()
    }

    private fun selectPaymentEventId() = databaseClient.sql(LAST_INSERT_ID_QUERY)
        .fetch()
        .first()
        .map { (it["LAST_INSERT_ID()"] as BigInteger).toLong() }

    private fun insertPaymentOrders(
        paymentEvent: PaymentEvent,
        paymentEventId: Long
    ): Mono<Long> {
        val valueClauses = paymentEvent.paymentOrders.joinToString(", ") { paymentOrder ->
            "($paymentEventId, ${paymentOrder.sellerId}, '${paymentOrder.orderId}', ${paymentOrder.productId}, ${paymentOrder.amount}, '${paymentOrder.paymentStatus}')"
        }

        return databaseClient.sql(INSERT_PAYMENT_ORDER_QUERY(valueClauses))
            .fetch()
            .rowsUpdated()
    }

    companion object {
        val INSERT_PAYMENT_EVENT_QUERY =
            """
            INSERT INTO payment_events (buyer_id, order_name, order_id)
            VALUES (:buyerId, :orderName, :orderId) 
            """.trimIndent()

        val LAST_INSERT_ID_QUERY =
            """
            SELECT LAST_INSERT_ID()
            """.trimIndent()

        val INSERT_PAYMENT_ORDER_QUERY = fun(valueClauses: String) =
            """
            INSERT INTO payment_orders (payment_event_id, seller_id, order_id, product_id, amount, payment_order_status) 
            VALUES $valueClauses
            """.trimIndent()
    }
}
