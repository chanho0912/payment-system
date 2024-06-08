package com.noah.paymentsystem.payment.application.service

import com.noah.paymentsystem.common.Usecase
import com.noah.paymentsystem.payment.application.domain.CheckoutResult
import com.noah.paymentsystem.payment.application.domain.PaymentEvent
import com.noah.paymentsystem.payment.application.domain.PaymentOrder
import com.noah.paymentsystem.payment.application.domain.PaymentStatus.NOT_STARTED
import com.noah.paymentsystem.payment.application.port.`in`.CheckoutCommand
import com.noah.paymentsystem.payment.application.port.`in`.CheckoutUsecase
import com.noah.paymentsystem.payment.application.port.out.LoadProductPort
import com.noah.paymentsystem.payment.application.port.out.SavePaymentPort
import reactor.core.publisher.Mono

@Usecase
class CheckoutService(
    private val loadProductPort: LoadProductPort,
    private val savePaymentPort: SavePaymentPort
) : CheckoutUsecase {
    override fun checkout(command: CheckoutCommand): Mono<CheckoutResult> {
        return loadProductPort.getProduct(command.cardId, command.productIds).collectList()
            .map { products ->
                PaymentEvent(
                    buyerId = command.buyerId,
                    orderId = command.idempotencyKey,
                    orderName = products.joinToString { it.name },
                    paymentOrders = products.map { product ->
                        PaymentOrder(
                            sellerId = product.sellerId,
                            orderId = command.idempotencyKey,
                            productId = product.id,
                            amount = product.amount,
                            buyerId = command.buyerId,
                            paymentStatus = NOT_STARTED
                        )
                    }
                )
            }
            .flatMap {
                savePaymentPort.savePayment(it).thenReturn(it)
            }
            .map {
                CheckoutResult(
                    orderId = it.orderId,
                    orderName = it.orderName,
                    amount = it.totalAmount()
                )
            }
    }
}
