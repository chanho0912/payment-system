package com.noah.paymentsystem.payment.adapter.out.persistence.repository

import reactor.core.publisher.Mono

interface PaymentValidationRepository {
    fun isValid(orderId: String, amount: Long): Mono<Boolean>
}
