package com.noah.paymentsystem.payment.application.service

import com.noah.paymentsystem.payment.adapter.out.persistence.exception.PaymentAlreadyProcessedException
import com.noah.paymentsystem.payment.adapter.out.persistence.exception.PaymentValidationException
import com.noah.paymentsystem.payment.adapter.out.web.toss.exception.PSPConfirmationException
import com.noah.paymentsystem.payment.application.domain.PaymentFailure
import com.noah.paymentsystem.payment.application.domain.PaymentStatus.FAILURE
import com.noah.paymentsystem.payment.application.domain.PaymentStatus.UNKNOWN
import com.noah.paymentsystem.payment.application.port.`in`.PaymentConfirmCommand
import com.noah.paymentsystem.payment.application.port.`in`.PaymentConfirmationResult
import com.noah.paymentsystem.payment.application.port.out.PaymentStatusUpdateCommand
import com.noah.paymentsystem.payment.application.port.out.PaymentStatusUpdatePort
import io.netty.handler.timeout.TimeoutException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class PaymentErrorHandler(
    private val paymentStatusUpdatePort: PaymentStatusUpdatePort
) {
    fun handlePaymentError(
        error: Throwable?,
        command: PaymentConfirmCommand
    ): Mono<PaymentConfirmationResult> {
        val (status, failure) = when (error) {
            is PSPConfirmationException -> error.paymentStatus() to PaymentFailure(
                error.errorCode,
                error.errorMessage
            )

            is PaymentValidationException ->
                FAILURE to PaymentFailure(
                    error::class.simpleName ?: "",
                    error.message ?: ""
                )

            is PaymentAlreadyProcessedException -> return Mono.just(
                PaymentConfirmationResult(
                    status = error.status,
                    failure = PaymentFailure(
                        error::class.simpleName ?: "",
                        error.message ?: ""
                    )
                )
            )

            is TimeoutException -> UNKNOWN to PaymentFailure(
                error::class.simpleName ?: "",
                error.message ?: ""
            )

            else -> UNKNOWN to PaymentFailure(
                error!!::class.simpleName ?: "",
                error.message ?: ""
            )
        }

        val command = PaymentStatusUpdateCommand(
            paymentKey = command.paymentKey,
            orderId = command.orderId,
            status = status,
            paymentFailure = failure
        )

        return paymentStatusUpdatePort.updatePaymentStatus(command)
            .map {
                PaymentConfirmationResult(
                    status = status,
                    failure = failure
                )
            }
    }
}
