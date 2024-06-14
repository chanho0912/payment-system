package com.noah.paymentsystem.payment.application.service

import com.noah.paymentsystem.common.Usecase
import com.noah.paymentsystem.payment.application.port.`in`.PaymentConfirmCommand
import com.noah.paymentsystem.payment.application.port.`in`.PaymentConfirmUsecase
import com.noah.paymentsystem.payment.application.port.`in`.PaymentConfirmationResult
import com.noah.paymentsystem.payment.application.port.out.PaymentExecutorPort
import com.noah.paymentsystem.payment.application.port.out.PaymentStatusUpdateCommand
import com.noah.paymentsystem.payment.application.port.out.PaymentStatusUpdatePort
import com.noah.paymentsystem.payment.application.port.out.PaymentValidationPort
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
    }
}
