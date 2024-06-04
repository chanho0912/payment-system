package com.noah.paymentsystem.payment.adapter.out.web.excutor

import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono


@Component
class TossPaymentExecutor(
    private val tossPaymentWebClient: WebClient,
) {

    fun execute(
        paymentKey: String,
        orderId: String,
        amount: String
    ): Mono<String> {
        return tossPaymentWebClient.post()
            .uri(uri)
            .bodyValue(
                TossPaymentConfirmRequest(
                    paymentKey = paymentKey,
                    orderId = orderId,
                    amount = amount)
            )
            .retrieve()
            .onStatus({ it.is4xxClientError || it.is5xxServerError }) { response ->
                response.bodyToMono(String::class.java)
                    .flatMap { Mono.error(RuntimeException(it)) }
            }
            .bodyToMono(String::class.java)
    }

    companion object {
        private const val uri = "/v1/payments/confirm"
    }

}

data class TossPaymentConfirmRequest(
    val paymentKey: String,
    val orderId: String,
    val amount: String
)
