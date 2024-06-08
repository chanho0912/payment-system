package com.noah.paymentsystem.payment.adapter.out.persistence.repository

import com.noah.paymentsystem.payment.application.domain.PaymentEvent
import reactor.core.publisher.Mono

fun interface PaymentRepository {
    fun save(paymentEvent: PaymentEvent): Mono<Void>
}
