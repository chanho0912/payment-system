package com.noah.paymentsystem.payment.adapter.out.persistence.repository

import com.noah.paymentsystem.payment.application.domain.PaymentEventMessage
import com.noah.paymentsystem.payment.application.domain.PaymentEventMessageType
import com.noah.paymentsystem.payment.application.port.out.PaymentStatusUpdateCommand
import reactor.core.publisher.Mono

interface PaymentOutboxRepository {
    fun insertOutbox(command: PaymentStatusUpdateCommand): Mono<PaymentEventMessage>
    fun markMessageAsSent(idempotencyKey: String, type: PaymentEventMessageType): Mono<Boolean>
    fun markMessageAsFailure(idempotencyKey: String, type: PaymentEventMessageType): Mono<Boolean>
}
