package com.noah.paymentsystem.payment.adapter.out.persistence.repository

import com.noah.paymentsystem.payment.adapter.out.persistence.exception.PaymentAlreadyProcessedException
import com.noah.paymentsystem.payment.application.domain.PaymentStatus
import com.noah.paymentsystem.payment.application.port.out.PaymentStatusUpdateCommand
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.reactive.TransactionalOperator
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Repository
class R2DBCPaymentStatusUpdateRepository(
    private val databaseClient: DatabaseClient,
    private val transactionalOperator: TransactionalOperator
) : PaymentStatusUpdateRepository {
    override fun updatePaymentStatusToExecuting(orderId: String, paymentKey: String): Mono<Boolean> {
        return checkPreviousPaymentOrderStatus(orderId)
            .flatMap {
                insertPaymentHistory(it, PaymentStatus.EXECUTING, "PAYMENT_CONFIRMATION_START")
            }
            .flatMap {
                updatePaymentOrderStatus(orderId, PaymentStatus.EXECUTING)
            }
            .flatMap {
                updatePaymentKey(orderId, paymentKey)
            }
            .`as` {
                transactionalOperator.transactional(it)
            }
            .thenReturn(true)
    }

    override fun updatePaymentStatus(paymentStatusUpdateCommand: PaymentStatusUpdateCommand): Mono<Boolean> {
        TODO()
    }

    private fun updatePaymentKey(orderId: String, paymentKey: String): Mono<Long> {
        return databaseClient.sql(
            """
                UPDATE payment_events
                SET payment_key = :paymentKey
                WHERE order_id = :orderId
            """.trimIndent()
        )
            .bind("paymentKey", paymentKey)
            .bind("orderId", orderId)
            .fetch()
            .rowsUpdated()
    }

    private fun updatePaymentOrderStatus(orderId: String, status: PaymentStatus): Mono<Long> {
        return databaseClient.sql(
            """
                UPDATE payment_orders
                SET payment_order_status = :status, updated_at = current_timestamp
                WHERE order_id = :orderId
            """.trimIndent()
        )
            .bind("status", status.name)
            .bind("orderId", orderId)
            .fetch()
            .rowsUpdated()
    }

    private fun checkPreviousPaymentOrderStatus(orderId: String): Mono<List<Pair<Long, String>>> {
        return selectPaymentOrderStatus(orderId)
            .handle { paymentOrder, sink ->
                when (paymentOrder.second) {
                    PaymentStatus.NOT_STARTED.name, PaymentStatus.EXECUTING.name, PaymentStatus.UNKNOWN.name ->
                        sink.next(paymentOrder)

                    PaymentStatus.SUCCESS.name ->
                        sink.error(
                            PaymentAlreadyProcessedException(
                                message = "이미 처리 성공한 결제 입니다.",
                                status = PaymentStatus.SUCCESS
                            )
                        )

                    PaymentStatus.FAILURE.name ->
                        sink.error(
                            PaymentAlreadyProcessedException(
                                message = "이미 처리 실패한 결제 입니다.",
                                status = PaymentStatus.FAILURE
                            )
                        )
                }
            }
            .collectList()
    }

    private fun selectPaymentOrderStatus(orderId: String): Flux<Pair<Long, String>> {
        return databaseClient.sql(
            """
                SELECT id, payment_order_status
                FROM payment_orders
                WHERE order_id = :orderId
            """.trimIndent()
        )
            .bind("orderId", orderId)
            .fetch()
            .all()
            .map {
                Pair(it["id"] as Long, it["payment_order_status"] as String)
            }
    }

    private fun insertPaymentHistory(
        paymentOrderIdToStatus: List<Pair<Long, String>>,
        status: PaymentStatus,
        reason: String
    ): Mono<Long> {
        if (paymentOrderIdToStatus.isEmpty()) {
            return Mono.empty()
        }

        val valueClauses = paymentOrderIdToStatus.joinToString(", ") {
            "( ${it.first}, '${it.second}', '$status', '$reason' )"
        }

        return databaseClient.sql(INSERT_PAYMENT_HISTORY_QUERY(valueClauses))
            .fetch()
            .rowsUpdated()
    }

    companion object {
        val INSERT_PAYMENT_HISTORY_QUERY = fun(valueClauses: String) =
            """
            INSERT INTO payment_order_histories (payment_order_id, payment_order_status, reason)
            VALUES $valueClauses
            """.trimIndent()
    }

}
