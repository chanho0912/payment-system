package com.noah.paymentsystem.payment.test

import com.noah.paymentsystem.payment.application.domain.PaymentEvent
import com.noah.paymentsystem.payment.application.domain.PaymentOrder
import com.noah.paymentsystem.payment.application.domain.PaymentStatus
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.math.BigDecimal

class R2DBCPaymentDatabaseHelper(
    private val databaseClient: DatabaseClient,
    private val transactionalOperator: TransactionalOperator
) : PaymentDatabaseHelper {
    override fun getPayments(orderId: String): PaymentEvent? {

        return databaseClient.sql(
            """
                SELECT * FROM payment_events pe
                INNER JOIN payment_orders po ON pe.order_id = po.order_id
                WHERE pe.order_id = :orderId
            """.trimIndent()
        )
            .bind("orderId", orderId)
            .fetch()
            .all()
            .groupBy { it["payment_event_id"] as Long }
            .flatMap { groupedFlux ->
                groupedFlux.collectList().map {
                    PaymentEvent(
                        id = groupedFlux.key(),
                        orderId = it[0]["order_id"] as String,
                        orderName = it[0]["order_name"] as String,
                        buyerId = it[0]["buyer_id"] as Long,
                        isPaymentDone = it[0]["is_payment_done"] as Byte == 1.toByte(),
                        paymentOrders = it.map { row ->
                            PaymentOrder(
                                id = row["id"] as Long,
                                paymentEventId = groupedFlux.key(),
                                productId = row["product_id"] as Long,
                                sellerId = row["seller_id"] as Long,
                                orderId = row["order_id"] as String,
                                amount = row["amount"] as BigDecimal,
                                paymentStatus = PaymentStatus.of(row["payment_order_status"] as String),
                                isLedgerUpdated = row["ledger_updated"] as Byte == 1.toByte(),
                                isWalletUpdated = row["wallet_updated"] as Byte == 1.toByte()
                            )
                        }
                    )
                }
            }
            .toMono()
            .block()
    }

    override fun clean(): Mono<Void> {
        return databaseClient.sql(
            """
                DELETE FROM payment_orders;
                DELETE FROM payment_events;
            """.trimIndent()
        )
            .fetch()
            .rowsUpdated()
            .`as`(transactionalOperator::transactional).then()
    }
}
