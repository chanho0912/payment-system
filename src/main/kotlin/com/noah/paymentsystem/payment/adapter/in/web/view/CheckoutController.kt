package com.noah.paymentsystem.payment.adapter.`in`.web.view

import com.noah.paymentsystem.common.IdempotencyCreator
import com.noah.paymentsystem.common.WebAdapter
import com.noah.paymentsystem.payment.adapter.`in`.web.request.CheckoutRequest
import com.noah.paymentsystem.payment.application.port.`in`.CheckoutCommand
import com.noah.paymentsystem.payment.application.port.`in`.CheckoutUsecase
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono

@Controller
@WebAdapter
class CheckoutController(
    private val checkoutUsecase: CheckoutUsecase
) {

    @GetMapping("/")
    fun checkout(checkoutRequest: CheckoutRequest, model: Model): Mono<String> {
        val command = CheckoutCommand(
            cardId = checkoutRequest.cardId, productIds = checkoutRequest.productIds,
            buyerId = checkoutRequest.buyerId, idempotencyKey = IdempotencyCreator.create(checkoutRequest.seed)
        )

        return checkoutUsecase.checkout(command)
            .map {
                model.addAttribute("amount", it.amount)
                model.addAttribute("orderId", it.orderId)
                model.addAttribute("orderName", it.orderName)
                "checkout"
            }
    }
}
