package com.noah.paymentsystem.payment.adapter.`in`.web.api

import com.noah.paymentsystem.common.WebAdapter
import com.noah.paymentsystem.payment.adapter.`in`.web.request.TossPaymentRequest
import com.noah.paymentsystem.payment.adapter.`in`.web.response.ApiResponse
import com.noah.paymentsystem.payment.adapter.out.web.toss.excutor.TossPaymentExecutor
import com.noah.paymentsystem.payment.application.port.`in`.PaymentConfirmCommand
import com.noah.paymentsystem.payment.application.port.`in`.PaymentConfirmUsecase
import com.noah.paymentsystem.payment.application.port.`in`.PaymentConfirmationResult
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
    private val paymentConfirmUsecase: PaymentConfirmUsecase
) {

    @PostMapping("/confirm")
    fun confirm(
        @RequestBody
        tossPaymentRequest: TossPaymentRequest
    ): Mono<ResponseEntity<ApiResponse<PaymentConfirmationResult>>> {

        val command = PaymentConfirmCommand(
            paymentKey = tossPaymentRequest.paymentKey,
            orderId = tossPaymentRequest.orderId,
            amount = tossPaymentRequest.amount
        )

        return paymentConfirmUsecase.confirm(command)
            .map {
                ResponseEntity.ok(
                    ApiResponse.with(HttpStatus.OK, "ok", it)
                )
            }
    }
}
