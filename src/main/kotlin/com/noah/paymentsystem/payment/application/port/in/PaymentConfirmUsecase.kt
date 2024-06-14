package com.noah.paymentsystem.payment.application.port.`in`

import com.noah.paymentsystem.payment.application.domain.PaymentFailure
import com.noah.paymentsystem.payment.application.domain.PaymentStatus
import reactor.core.publisher.Mono

interface PaymentConfirmUsecase {
    fun confirm(command: PaymentConfirmCommand): Mono<PaymentConfirmationResult>
}

data class PaymentConfirmCommand(
    val paymentKey: String,
    val orderId: String,
    val amount: Long
)

data class PaymentConfirmationResult(
    val status: PaymentStatus,
    val failure: PaymentFailure? = null
) {
    init {
        if (status == PaymentStatus.FAILURE) {
            require(failure != null) {
                "failure should not be null when status is FAILURE"
            }
        }
    }

    val message = when (status) {
        PaymentStatus.SUCCESS -> "Payment is successful"
        PaymentStatus.FAILURE -> "Payment is failed"
        PaymentStatus.UNKNOWN -> "Payment status is unknown"
        else -> error("Payment status is invalid")
    }
}
