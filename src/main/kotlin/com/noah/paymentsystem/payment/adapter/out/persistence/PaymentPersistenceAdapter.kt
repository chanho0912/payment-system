package com.noah.paymentsystem.payment.adapter.out.persistence

import com.noah.paymentsystem.common.PersistenceAdapter
import com.noah.paymentsystem.payment.adapter.out.persistence.repository.PaymentRepository
import com.noah.paymentsystem.payment.application.domain.PaymentEvent
import com.noah.paymentsystem.payment.application.port.out.SavePaymentPort
import reactor.core.publisher.Mono

@PersistenceAdapter
class PaymentPersistenceAdapter(
    private val paymentRepository: PaymentRepository
) : SavePaymentPort {
    override fun savePayment(paymentEvent: PaymentEvent): Mono<Void> {
        return paymentRepository.save(paymentEvent)
    }

}
