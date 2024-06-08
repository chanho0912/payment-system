package com.noah.paymentsystem.payment.application.port.`in`

import com.noah.paymentsystem.payment.application.domain.CheckoutResult
import reactor.core.publisher.Mono

interface CheckoutUsecase {
    fun checkout(command: CheckoutCommand): Mono<CheckoutResult>
}

data class CheckoutCommand(
    val cardId: Long,
    val productIds: List<Long>,
    val buyerId: Long,
    val idempotencyKey: String
)
