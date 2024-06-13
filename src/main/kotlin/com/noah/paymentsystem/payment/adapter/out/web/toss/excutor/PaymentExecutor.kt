package com.noah.paymentsystem.payment.adapter.out.web.toss.excutor

import com.noah.paymentsystem.payment.application.port.`in`.PaymentConfirmCommand
import com.noah.paymentsystem.payment.application.port.out.PaymentExecutionResult
import reactor.core.publisher.Mono

interface PaymentExecutor {
    fun execute(command: PaymentConfirmCommand): Mono<PaymentExecutionResult>
}
