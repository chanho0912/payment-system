package com.noah.paymentsystem.payment.application.domain

data class CheckoutResult(
    val amount: Long,
    val orderId: String,
    val orderName: String
)
