package com.noah.paymentsystem.payment.adapter.`in`.web.view

import com.noah.paymentsystem.common.WebAdapter
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono

@Controller
@WebAdapter
class PaymentController {

    @GetMapping("/success")
    fun success(): Mono<String> = Mono.just("success")

    @GetMapping("/fail")
    fun fail(): Mono<String> = Mono.just("fail")
}
