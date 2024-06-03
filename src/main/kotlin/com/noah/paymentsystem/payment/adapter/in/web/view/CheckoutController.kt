package com.noah.paymentsystem.payment.adapter.`in`.web.view

import com.noah.paymentsystem.common.WebAdapter
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import reactor.core.publisher.Mono

@Controller
@WebAdapter
class CheckoutController {

    @RequestMapping("/")
    fun checkout(): Mono<String> = Mono.just("checkout")
}
