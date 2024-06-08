package com.noah.paymentsystem.payment.adapter.out.web.toss.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ClientHttpConnector
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import reactor.netty.resources.ConnectionProvider
import java.util.Base64

@Configuration
class TossWebClientConfiguration(
    @Value("\${PSP.toss.url}") private val bassUrl: String,
    @Value("\${PSP.toss.secretKey}") private val secretKey: String
) {
    @Bean
    fun tossPaymentWebClient(): WebClient {
        val encodedSecretKey = Base64.getEncoder().encodeToString(("$secretKey:").toByteArray())
        val authorization = "Basic $encodedSecretKey"

        return WebClient.builder()
            .baseUrl(bassUrl)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.AUTHORIZATION, authorization)
            .clientConnector(reactorClientHttpConnector())
            .build()
    }

    private fun reactorClientHttpConnector(): ClientHttpConnector {
        return ReactorClientHttpConnector(
            HttpClient.create(
                ConnectionProvider.builder("toss-payment").build()
            )
        )
    }
}

