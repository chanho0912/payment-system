package com.noah.paymentsystem.payment.adapter.out.persistence.repository

import com.noah.paymentsystem.payment.adapter.out.persistence.exception.PaymentValidationException
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.math.BigDecimal

@Repository
class R2DBCPaymentValidationRepository(
    private val databaseClient: DatabaseClient
) : PaymentValidationRepository {
    override fun isValid(orderId: String, amount: Long): Mono<Boolean> {
        return databaseClient.sql(
            """
                SELECT SUM(amount) as total_amount 
                FROM payment_orders 
                WHERE order_id = :orderId
            """.trimIndent()
        )
            .bind("orderId", orderId)
            .fetch()
            .first()
            .handle { row, sink ->
                if ((row["total_amount"] as BigDecimal).toLong() == amount) {
                    sink.next(true)
                } else {
                    sink.error(
                        PaymentValidationException("결제 (orderId: $orderId)에서 금액(amount: $amount)이 일치하지 않습니다.")
                    )
                }
            }
    }
}
