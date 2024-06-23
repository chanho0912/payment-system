package com.noah.paymentsystem.payment.application.port.out

import com.noah.paymentsystem.payment.application.domain.PaymentFailure
import com.noah.paymentsystem.payment.application.domain.PaymentStatus
import com.noah.paymentsystem.payment.application.domain.PaymentStatus.FAILURE
import reactor.core.publisher.Mono

interface PaymentStatusUpdatePort {
    fun updatePaymentStatusToExecuting(orderId: String, paymentKey: String): Mono<Boolean>

    fun updatePaymentStatus(command: PaymentStatusUpdateCommand): Mono<Boolean>
}

data class PaymentStatusUpdateCommand(
    val orderId: String,
    val paymentKey: String,
    val status: PaymentStatus,
    val paymentExtraDetails: PaymentExtraDetails? = null,
    val paymentFailure: PaymentFailure? = null
) {

    constructor(paymentExecutionResult: PaymentExecutionResult) : this(
        paymentKey = paymentExecutionResult.paymentKey,
        orderId = paymentExecutionResult.orderId,
        status = paymentExecutionResult.paymentStatus(),
        paymentExtraDetails = paymentExecutionResult.extraDetails,
        paymentFailure = paymentExecutionResult.paymentFailure
    )

    init {
        require(status == PaymentStatus.SUCCESS || status == FAILURE || status == PaymentStatus.UNKNOWN) {
            "status should be one of SUCCESS, FAILURE, UNKNOWN"
        }

        if (status == PaymentStatus.SUCCESS) {
            require(paymentExtraDetails != null) {
                "paymentExtraDetails should not be null when status is SUCCESS"
            }
        } else if (status == FAILURE) {
            require(paymentFailure != null) {
                "paymentFailure should not be null when status is FAILURE"
            }
        }
    }
}
