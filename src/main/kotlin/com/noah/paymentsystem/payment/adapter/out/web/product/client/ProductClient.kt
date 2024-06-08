package com.noah.paymentsystem.payment.adapter.out.web.product.client

import com.noah.paymentsystem.payment.application.domain.Product
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

interface ProductClient {
    fun getProducts(cardId: Long, productIds: List<Long>): Flux<Product>
}

@Component
class FakeProductClient : ProductClient {
    override fun getProducts(cardId: Long, productIds: List<Long>): Flux<Product> {
        return Flux.fromIterable(
            productIds.map {
                Product(
                    id = it,
                    amount = (it * 10000).toBigDecimal(),
                    name = "Test Product $it",
                    sellerId = 1,
                    quantity = 2
                )
            }
        )
    }
}
