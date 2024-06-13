package com.noah.paymentsystem.payment.adapter.out.web.toss

import com.noah.paymentsystem.common.WebAdapter
import com.noah.paymentsystem.payment.adapter.out.web.toss.excutor.PaymentExecutor
import com.noah.paymentsystem.payment.application.port.`in`.PaymentConfirmCommand
import com.noah.paymentsystem.payment.application.port.out.PaymentExecutionResult
import com.noah.paymentsystem.payment.application.port.out.PaymentExecutorPort
import reactor.core.publisher.Mono

@WebAdapter
class PaymentExecutorWebAdapter(
    private val paymentExecutor: PaymentExecutor
) : PaymentExecutorPort {
    override fun execute(command: PaymentConfirmCommand): Mono<PaymentExecutionResult> {
        return paymentExecutor.execute(command)
    }
}
