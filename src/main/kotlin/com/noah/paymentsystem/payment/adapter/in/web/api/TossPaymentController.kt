package com.noah.paymentsystem.payment.adapter.`in`.web.api

import com.noah.paymentsystem.common.WebAdapter
import com.noah.paymentsystem.payment.adapter.`in`.web.request.TossPaymentRequest
import com.noah.paymentsystem.payment.adapter.`in`.web.response.ApiResponse
import com.noah.paymentsystem.payment.adapter.out.web.toss.excutor.TossPaymentExecutor
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@WebAdapter
@RestController
@RequestMapping("v1/toss")
class TossPaymentController(
    private val paymentExecutor: TossPaymentExecutor
) {

    @PostMapping("/confirm")
    fun confirm(
        @RequestBody
        tossPaymentRequest: TossPaymentRequest
    ): Mono<ResponseEntity<ApiResponse<String>>> {

        val confirmation = paymentExecutor.execute(
            tossPaymentRequest.paymentKey,
            tossPaymentRequest.orderId,
            tossPaymentRequest.amount.toString()
        )
        return confirmation
            .map {
                ResponseEntity.ok(
                    ApiResponse.with(HttpStatus.OK, "ok", it)
                )
            }
    }
}
