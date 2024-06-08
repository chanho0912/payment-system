package com.noah.paymentsystem.payment.application.port.out

import com.noah.paymentsystem.payment.application.domain.PaymentEvent
import reactor.core.publisher.Mono

interface SavePaymentPort {
    fun savePayment(paymentEvent: PaymentEvent): Mono<Void>
}
