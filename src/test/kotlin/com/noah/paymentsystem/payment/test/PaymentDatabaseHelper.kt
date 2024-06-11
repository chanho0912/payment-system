package com.noah.paymentsystem.payment.test

import com.noah.paymentsystem.payment.application.domain.PaymentEvent
import reactor.core.publisher.Mono

interface PaymentDatabaseHelper {
    fun getPayments(orderId: String): PaymentEvent?

    fun clean(): Mono<Void>
}
