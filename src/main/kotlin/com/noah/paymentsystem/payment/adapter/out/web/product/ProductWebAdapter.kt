package com.noah.paymentsystem.payment.adapter.out.web.product

import com.noah.paymentsystem.common.WebAdapter
import com.noah.paymentsystem.payment.adapter.out.web.product.client.ProductClient
import com.noah.paymentsystem.payment.application.domain.Product
import com.noah.paymentsystem.payment.application.port.out.LoadProductPort
import reactor.core.publisher.Flux

@WebAdapter
class ProductWebAdapter(
    private val productClient: ProductClient
) : LoadProductPort {
    override fun getProduct(cardId: Long, productIds: List<Long>): Flux<Product> {
        return productClient.getProducts(cardId, productIds)
    }

}
