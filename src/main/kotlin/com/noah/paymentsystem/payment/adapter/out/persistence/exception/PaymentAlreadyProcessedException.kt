package com.noah.paymentsystem.payment.adapter.out.persistence.exception

import com.noah.paymentsystem.payment.application.domain.PaymentStatus

class PaymentAlreadyProcessedException(
    val status: PaymentStatus,
    message: String
) : RuntimeException(message)
