package com.noah.paymentsystem.payment.application.domain

enum class PaymentMethod(description: String) {
    EASY_PAY("간편 결제");

    companion object {
        fun from(method: String): PaymentMethod {
            return entries.find { it.name == method } ?: error("결제 방식 (method: $method) 는 올바르지 않은 방식입니다.")
        }
    }
}
