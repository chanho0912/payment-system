package com.noah.paymentsystem.payment.application.service

import com.noah.paymentsystem.common.Usecase
import com.noah.paymentsystem.payment.adapter.out.persistence.exception.PaymentAlreadyProcessedException
import com.noah.paymentsystem.payment.adapter.out.persistence.exception.PaymentValidationException
import com.noah.paymentsystem.payment.adapter.out.web.toss.exception.PSPConfirmationException
import com.noah.paymentsystem.payment.application.domain.PaymentFailure
import com.noah.paymentsystem.payment.application.domain.PaymentStatus
import com.noah.paymentsystem.payment.application.domain.PaymentStatus.FAILURE
import com.noah.paymentsystem.payment.application.domain.PaymentStatus.UNKNOWN
import com.noah.paymentsystem.payment.application.port.`in`.PaymentConfirmCommand
import com.noah.paymentsystem.payment.application.port.`in`.PaymentConfirmUsecase
import com.noah.paymentsystem.payment.application.port.`in`.PaymentConfirmationResult
import com.noah.paymentsystem.payment.application.port.out.PaymentExecutorPort
import com.noah.paymentsystem.payment.application.port.out.PaymentStatusUpdateCommand
import com.noah.paymentsystem.payment.application.port.out.PaymentStatusUpdatePort
import com.noah.paymentsystem.payment.application.port.out.PaymentValidationPort
import io.netty.handler.timeout.TimeoutException
import reactor.core.publisher.Mono

@Usecase
class PaymentConfirmService(
    private val paymentStatusUpdatePort: PaymentStatusUpdatePort,
    private val paymentValidationPort: PaymentValidationPort,
    private val paymentExecutorPort: PaymentExecutorPort
) : PaymentConfirmUsecase {
    override fun confirm(command: PaymentConfirmCommand): Mono<PaymentConfirmationResult> {
        return paymentStatusUpdatePort.updatePaymentStatusToExecuting(command.orderId, command.paymentKey)
            .filterWhen {
                paymentValidationPort.isValid(command.orderId, command.amount)
            }
            .flatMap {
                paymentExecutorPort.execute(command)
            }
            .flatMap {
                paymentStatusUpdatePort.updatePaymentStatus(
                    PaymentStatusUpdateCommand(
                        orderId = command.orderId,
                        paymentKey = command.paymentKey,
                        status = it.paymentStatus(),
                        paymentExtraDetails = it.extraDetails,
                        paymentFailure = it.paymentFailure
                    )
                )
                    .thenReturn(it)
            }
            .map {
                PaymentConfirmationResult(
                    status = it.paymentStatus(),
                    failure = it.paymentFailure
                )
            }
            .onErrorResume { error ->
                handlePaymentError(error, command)
            }
    }

    private fun handlePaymentError(
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
