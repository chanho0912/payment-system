package com.noah.paymentsystem.payment.adapter.out.persistence

import com.noah.paymentsystem.common.PersistenceAdapter
import com.noah.paymentsystem.payment.adapter.out.persistence.repository.PaymentRepository
import com.noah.paymentsystem.payment.adapter.out.persistence.repository.PaymentStatusUpdateRepository
import com.noah.paymentsystem.payment.adapter.out.persistence.repository.PaymentValidationRepository
import com.noah.paymentsystem.payment.application.domain.PaymentEvent
import com.noah.paymentsystem.payment.application.port.out.PaymentStatusUpdateCommand
import com.noah.paymentsystem.payment.application.port.out.PaymentStatusUpdatePort
import com.noah.paymentsystem.payment.application.port.out.PaymentValidationPort
import com.noah.paymentsystem.payment.application.port.out.SavePaymentPort
import reactor.core.publisher.Mono

@PersistenceAdapter
class PaymentPersistenceAdapter(
    private val paymentRepository: PaymentRepository,
    private val paymentStatusUpdateRepository: PaymentStatusUpdateRepository,
    private val paymentValidationRepository: PaymentValidationRepository
) : SavePaymentPort,
    PaymentStatusUpdatePort,
    PaymentValidationPort {
    override fun savePayment(paymentEvent: PaymentEvent): Mono<Void> {
        return paymentRepository.save(paymentEvent)
    }

    override fun updatePaymentStatusToExecuting(orderId: String, paymentKey: String): Mono<Boolean> {
        return paymentStatusUpdateRepository.updatePaymentStatusToExecuting(orderId, paymentKey)
    }

    override fun updatePaymentStatus(command: PaymentStatusUpdateCommand): Mono<Boolean> {
        return paymentStatusUpdateRepository.updatePaymentStatus(command)
    }

    override fun isValid(orderId: String, amount: Long): Mono<Boolean> {
        return paymentValidationRepository.isValid(orderId, amount)
    }

}
