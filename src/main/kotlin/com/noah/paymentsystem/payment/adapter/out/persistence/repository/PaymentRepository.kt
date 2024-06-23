package com.noah.paymentsystem.payment.adapter.out.persistence.repository

import com.noah.paymentsystem.payment.application.domain.PaymentEvent
import com.noah.paymentsystem.payment.application.domain.PendingPaymentEvent
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PaymentRepository {
    fun save(paymentEvent: PaymentEvent): Mono<Void>

    fun getPendingPayment(): Flux<PendingPaymentEvent>
}
