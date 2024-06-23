package com.noah.paymentsystem.payment.application.port.out

import com.noah.paymentsystem.payment.application.domain.PendingPaymentEvent
import reactor.core.publisher.Flux

interface LoadPendingPaymentPort {

    fun getPendingPayment(): Flux<PendingPaymentEvent>
}
