package com.noah.paymentsystem.payment.application.port.out

import com.noah.paymentsystem.payment.application.domain.PSPConfirmationStatus
import com.noah.paymentsystem.payment.application.domain.PaymentFailure
import com.noah.paymentsystem.payment.application.domain.PaymentMethod
import com.noah.paymentsystem.payment.application.domain.PaymentStatus
import com.noah.paymentsystem.payment.application.domain.PaymentType
import com.noah.paymentsystem.payment.application.port.`in`.PaymentConfirmCommand
import reactor.core.publisher.Mono
import java.time.LocalDateTime

interface PaymentExecutorPort {
    fun execute(command: PaymentConfirmCommand): Mono<PaymentExecutionResult>
}

data class PaymentExecutionResult(
    val orderId: String,
    val paymentKey: String,
    val extraDetails: PaymentExtraDetails? = null,
    val paymentFailure: PaymentFailure? = null,
    val isSuccess: Boolean,
    val isFailure: Boolean,
    val isUnknown: Boolean,
    val isRetryable: Boolean
) {
    fun paymentStatus(): PaymentStatus {
        return when {
            isSuccess -> PaymentStatus.SUCCESS
            isFailure -> PaymentStatus.FAILURE
            isUnknown -> PaymentStatus.UNKNOWN
            else -> error("Payment (orderId: $orderId) has invalid payment status")
        }
    }

    init {
        require(isSuccess xor isFailure xor isUnknown) {
            "Only one of isSuccess, isFailure, isUnknown should be true"
        }
    }
}

data class PaymentExtraDetails(
    val type: PaymentType,
    val method: PaymentMethod,
    val approvedAt: LocalDateTime,
    val orderName: String,
    val pspConfirmationStatus: PSPConfirmationStatus,
    val totalAmount: Long,
    val pspRawData: String
)


