package com.noah.paymentsystem.payment.application.port.`in`

import reactor.core.publisher.Mono

interface PaymentConfirmUsecase {
    fun confirm(command: PaymentConfirmCommand): Mono<PaymentConfirmationResult>
}

data class PaymentConfirmCommand(
    val paymentKey: String,
    val orderId: String,
    val amount: Long
)

class PaymentConfirmationResult
