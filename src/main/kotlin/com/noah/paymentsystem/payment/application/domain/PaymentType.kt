package com.noah.paymentsystem.payment.application.domain

enum class PaymentType(description: String) {
    NORMAL("일반 결제");

    companion object {
        fun from(type: String): PaymentType {
            return entries.find { it.name == type } ?: error("결제 타입 (type: $type) 는 올바르지 않은 타입입니다.")
        }
    }
}
