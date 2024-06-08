package com.noah.paymentsystem.payment.application.domain

import java.math.BigDecimal

data class PaymentOrder(
    val id: Long? = null,
    val paymentEventId: Long? = null,
    val sellerId: Long,
    val buyerId: Long,
    val productId: Long,
    val orderId: String,
    val amount: BigDecimal,
    val paymentStatus: PaymentStatus,
    private var isLedgerUpdated: Boolean = false,
    private var isWalletUpdated: Boolean = false
)
