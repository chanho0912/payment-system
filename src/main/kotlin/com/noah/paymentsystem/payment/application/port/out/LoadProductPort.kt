package com.noah.paymentsystem.payment.application.port.out

import com.noah.paymentsystem.payment.application.domain.Product
import reactor.core.publisher.Flux

fun interface LoadProductPort {
    fun getProduct(cardId: Long, productIds: List<Long>): Flux<Product>
}
