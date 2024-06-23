package com.noah.paymentsystem.payment.adapter.out.web.toss.exception

import com.noah.paymentsystem.payment.application.domain.PaymentStatus
import com.noah.paymentsystem.payment.application.port.`in`.PaymentConfirmationResult
import reactor.core.publisher.Mono

data class PSPConfirmationException(
    val errorCode: String,
    val errorMessage: String,
    val isSuccess: Boolean,
    val isFailure: Boolean,
    val isUnknown: Boolean,
    val isRetryableError: Boolean,
    override val cause: Throwable? = null
) : RuntimeException(errorMessage, cause) {
    fun paymentStatus(): PaymentStatus {
        return when {
            isSuccess -> PaymentStatus.SUCCESS
            isFailure -> PaymentStatus.FAILURE
            isUnknown -> PaymentStatus.UNKNOWN
            else -> error("Unknown PaymentStatus")
        }
    }
}
